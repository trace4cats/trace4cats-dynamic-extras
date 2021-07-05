package io.janstenpickle.trace4cats.`export`.dynamic

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import cats.effect.kernel.{Resource, Temporal}
import cats.kernel.Eq
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.Chunk
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.syntax._
import io.circe.{Decoder, DecodingFailure, Encoder}
import io.janstenpickle.trace4cats.`export`.ExportRetryConfig.NextDelay
import io.janstenpickle.trace4cats.`export`.{
  CompleterConfig,
  ExportRetryConfig,
  HotswapSpanCompleter,
  QueuedSpanCompleter
}
import io.janstenpickle.trace4cats.kernel.{SpanCompleter, SpanExporter}
import io.janstenpickle.trace4cats.model.TraceProcess
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

import scala.concurrent.duration.FiniteDuration

object CompleterHttpRoutes {
  implicit val circeConfig: Configuration = Configuration.default.withDefaults

  implicit val finiteDurationEncoder: Encoder[FiniteDuration] =
    Encoder.encodeDuration.contramap(dur => Duration.of(dur.toNanos, ChronoUnit.NANOS))
  implicit val finiteDurationDecoder: Decoder[FiniteDuration] =
    Decoder.decodeDuration.map(dur => FiniteDuration(dur.toNanos, TimeUnit.NANOSECONDS))

  private final val nextDelayField = "nextDelay"
  private final val exponentialDelay = "exponential"

  implicit val nextDelayEncoder: Encoder[NextDelay] = Encoder.instance {
    case NextDelay.Constant(delay) => Map(nextDelayField -> delay.asJson).asJson
    case NextDelay.Exponential => Map(nextDelayField -> exponentialDelay).asJson
  }
  implicit val nextDelayDecoder: Decoder[NextDelay] = Decoder.instance { cursor =>
    cursor
      .get[FiniteDuration](nextDelayField)
      .map(NextDelay.Constant)
      .widen
      .orElse(cursor.get[String](nextDelayField).flatMap {
        case `exponentialDelay` => Right(NextDelay.Exponential)
        case v => Left(DecodingFailure(s"Invalid next delay value '$v'", cursor.history))
      })
  }

  implicit val retryConfigEncoder: Encoder[ExportRetryConfig] = deriveConfiguredEncoder
  implicit val retryConfigDecoder: Decoder[ExportRetryConfig] = deriveConfiguredDecoder

  implicit val completerConfigEncoder: Encoder[CompleterConfig] = deriveConfiguredEncoder
  implicit val completerConfigDecoder: Decoder[CompleterConfig] = deriveConfiguredDecoder

  def apply[F[_]: Temporal: Logger](
    process: TraceProcess,
    exporter: SpanExporter[F, Chunk],
    config: CompleterConfig
  ): Resource[F, (SpanCompleter[F], HttpRoutes[F])] =
    impl(exporter, config)(QueuedSpanCompleter(process, _, _))

  def impl[F[_]: Temporal, G[_], A: Decoder: Encoder: Eq](exporter: SpanExporter[F, G], initialConfig: A)(
    makeCompleter: (SpanExporter[F, G], A) => Resource[F, SpanCompleter[F]]
  ): Resource[F, (SpanCompleter[F], HttpRoutes[F])] =
    HotswapSpanCompleter[F, A](initialConfig)(makeCompleter(exporter, _)).map { completer =>
      object dsl extends Http4sDsl[F]
      import dsl._

      (
        completer,
        HttpRoutes.of[F] {
          case GET -> Root / "completer" => Ok(completer.getConfig)
          case req @ POST -> Root / "completer" => Accepted(req.as[A].flatMap(completer.update))
        }
      )
    }
}
