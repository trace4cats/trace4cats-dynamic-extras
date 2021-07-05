package io.janstenpickle.trace4cats.sampling.dynamic.http

import cats.Applicative
import cats.effect.kernel.{Async, Resource}
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.{Router, Server}

import scala.concurrent.ExecutionContext

object HttpResource {
  def apply[F[_]: Async](
    routes: HttpRoutes[F],
    bindHost: String = "0.0.0.0",
    bindPort: Int = 8080,
    endpoint: String = "trace4cats",
    executionContext: Option[ExecutionContext] = None
  )(builder: BlazeServerBuilder[F] => BlazeServerBuilder[F] = identity): Resource[F, Server] =
    Resource
      .eval(executionContext.fold(Async[F].executionContext)(Applicative[F].pure))
      .flatMap(ec =>
        builder(BlazeServerBuilder[F](ec))
          .bindHttp(port = bindPort, host = bindHost)
          .withHttpApp(Router(endpoint -> routes).orNotFound)
          .resource
      )
}
