package io.janstenpickle.trace4cats.`export`.dynamic

import cats.effect.kernel.{Resource, Temporal}
import cats.kernel.Eq
import cats.syntax.flatMap._
import io.circe.{Decoder, Encoder}
import io.janstenpickle.trace4cats.`export`.HotswapSpanExporter
import io.janstenpickle.trace4cats.kernel.SpanExporter
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

object ExporterHttpRoutes {
  def impl[F[_]: Temporal, G[_], A: Decoder: Encoder: Eq](
    initialConfig: A
  )(makeExporter: A => Resource[F, SpanExporter[F, G]]): Resource[F, (SpanExporter[F, G], HttpRoutes[F])] =
    HotswapSpanExporter[F, G, A](initialConfig)(makeExporter).map { exporter =>
      object dsl extends Http4sDsl[F]
      import dsl._

      (
        exporter,
        HttpRoutes.of[F] {
          case GET -> Root / "exporter" => Ok(exporter.getConfig)
          case req @ POST -> Root / "exporter" => Accepted(req.as[A].flatMap(exporter.update))
        }
      )
    }
}
