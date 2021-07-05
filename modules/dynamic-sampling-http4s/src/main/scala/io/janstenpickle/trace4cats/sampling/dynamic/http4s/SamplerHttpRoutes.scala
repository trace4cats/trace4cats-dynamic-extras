package io.janstenpickle.trace4cats.sampling.dynamic.http4s

import cats.effect.kernel.{Resource, Temporal}
import cats.kernel.Eq
import cats.syntax.flatMap._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import io.circe.{Codec, Decoder, Encoder}
import io.janstenpickle.trace4cats.kernel.SpanSampler
import io.janstenpickle.trace4cats.sampling.dynamic.HotSwapSpanSampler
import io.janstenpickle.trace4cats.sampling.dynamic.config.{SamplerConfig, SamplerUtil}
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

object SamplerHttpRoutes {
  implicit val circeConfig: Configuration = Configuration.default.withDefaults.withDiscriminator("samplerType")
  implicit val configCodec: Codec[SamplerConfig] = deriveConfiguredCodec

  def apply[F[_]: Temporal](
    initialConfig: SamplerConfig = SamplerConfig.Never
  ): Resource[F, (SpanSampler[F], HttpRoutes[F])] =
    impl(initialConfig)(SamplerUtil.makeSampler[F])

  implicit def impl[F[_]: Temporal, A: Encoder: Decoder: Eq](initialConfig: A)(
    makeSampler: A => Resource[F, SpanSampler[F]]
  ): Resource[F, (SpanSampler[F], HttpRoutes[F])] = HotSwapSpanSampler[F, Option[A]](Some(initialConfig))(
    _.fold(SamplerUtil.makeSampler(SamplerConfig.Never))(makeSampler)
  ).map { sampler =>
    object dsl extends Http4sDsl[F]
    import dsl._

    (
      sampler,
      HttpRoutes.of[F] {
        case GET -> Root / "sampler" => Ok(sampler.getConfig)
        case req @ POST -> Root / "sampler" => Accepted(req.as[A].flatMap(a => sampler.swap(Some(a))))
        case POST -> Root / "killswitch" => Ok(sampler.swap(None))
      }
    )
  }
}
