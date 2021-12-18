package io.janstenpickle.trace4cats.sampling.dynamic.http

import cats.effect.kernel.{Async, Resource}
import cats.syntax.all._
import io.janstenpickle.trace4cats.kernel.SpanSampler
import io.janstenpickle.trace4cats.sampling.dynamic.config.SamplerConfig
import io.janstenpickle.trace4cats.sampling.dynamic.http4s.SamplerHttpRoutes
import org.http4s.HttpApp
import org.http4s.implicits._
import org.http4s.server.{Router, ServerBuilder}

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
