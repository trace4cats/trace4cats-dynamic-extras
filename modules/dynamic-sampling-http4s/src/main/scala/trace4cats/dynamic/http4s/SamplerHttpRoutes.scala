package trace4cats.dynamic.http4s

import cats.effect.kernel.{Resource, Temporal}
import cats.syntax.flatMap._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import trace4cats.dynamic.config.{ConfiguredHotSwapSpanSampler, SamplerConfig}
import trace4cats.dynamic.http4s.SamplerConfigCodec._
import trace4cats.kernel.SpanSampler

object SamplerHttpRoutes {

  def create[F[_]: Temporal](
    initialConfig: SamplerConfig = SamplerConfig.Never
  ): Resource[F, (SpanSampler[F], HttpRoutes[F])] =
    ConfiguredHotSwapSpanSampler[F](initialConfig).map { sampler =>
      object dsl extends Http4sDsl[F]
      import dsl._

      (
        sampler,
        HttpRoutes.of[F] {
          case GET -> Root / "config" => Ok(sampler.getConfig)
          case req @ POST -> Root / "config" => Accepted(req.as[SamplerConfig].flatMap(sampler.updateConfig))
          case POST -> Root / "killswitch" => Ok(sampler.updateConfig(SamplerConfig.Never))
        }
      )
    }
}
