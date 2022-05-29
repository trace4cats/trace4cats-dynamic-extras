package trace4cats.dynamic.http4s

import cats.effect.kernel.{Async, Resource}
import cats.syntax.all._
import org.http4s.HttpApp
import org.http4s.implicits._
import org.http4s.server.{Router, ServerBuilder}
import trace4cats.dynamic.config.SamplerConfig
import trace4cats.kernel.SpanSampler

object HttpDynamicSpanSampler {
  def build[F[_]: Async](
    builder: HttpApp[F] => ServerBuilder[F],
    endpoint: String = "trace4cats",
    initialConfig: SamplerConfig = SamplerConfig.Never
  ): Resource[F, SpanSampler[F]] = SamplerHttpRoutes.create[F](initialConfig).flatMap { case (sampler, routes) =>
    builder(Router(endpoint -> routes).orNotFound).resource
      .as(sampler)
  }
}
