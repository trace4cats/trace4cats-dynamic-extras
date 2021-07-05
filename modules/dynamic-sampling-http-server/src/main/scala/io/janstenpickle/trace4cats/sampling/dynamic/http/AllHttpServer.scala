package io.janstenpickle.trace4cats.sampling.dynamic.http

import cats.effect.kernel.{Async, Resource}
import cats.kernel.Eq
import fs2.Chunk
import io.circe.{Decoder, Encoder}
import io.janstenpickle.trace4cats.`export`.dynamic.{CompleterHttpRoutes, ExporterHttpRoutes}
import io.janstenpickle.trace4cats.kernel.{SpanCompleter, SpanExporter, SpanSampler}
import io.janstenpickle.trace4cats.sampling.dynamic.http4s.SamplerHttpRoutes
import cats.syntax.semigroupk._
import cats.syntax.traverse._
import io.janstenpickle.trace4cats.`export`.CompleterConfig
import io.janstenpickle.trace4cats.sampling.dynamic.config.SamplerConfig
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object AllHttpServer {
  def apply[F[_]: Async, G[_], S: Encoder: Decoder: Eq, C: Encoder: Decoder: Eq, E: Encoder: Decoder: Eq](
    sampler: Option[HotSwapConstructor[F, S, SpanSampler[F]]] = None,
    completer: Option[CompleterConstructor[F, G, C]] = None,
    exporter: Option[ExporterConstructor[F, G, E]] = None,
    bindHost: String = "0.0.0.0",
    bindPort: Int = 8080,
    endpoint: String = "trace4cats",
    executionContext: Option[ExecutionContext] = None
  )(
    builder: BlazeServerBuilder[F] => BlazeServerBuilder[F] = identity
  ): Resource[F, (Option[SpanSampler[F]], Option[SpanCompleter[F]], Option[SpanExporter[F, Chunk]])] =
    for {
      s <- sampler.traverse(s => SamplerHttpRoutes.impl(s.initialConfig)(s.make))
      e <- exporter.traverse(e => ExporterHttpRoutes.impl(e.initialConfig)(e.make))
      c <- completer.traverse(c => CompleterHttpRoutes.impl())
    } yield ()

//    impl[F](
//      sampler.traverse(s => SamplerHttpRoutes.impl(s.initialConfig)(s.make)),
//      completer.traverse(c => CompleterHttpRoutes.impl(c.initialConfig)(c.make)),
//      exporter.traverse(e => ExporterHttpRoutes.impl(e.initialConfig)(e.make)),
//      bindHost,
//      bindPort,
//      endpoint,
//      executionContext
//    )(builder)

  def impl[F[_]: Async](
    s: Option[(SpanSampler[F], HttpRoutes[F])],
    c: Option[(SpanCompleter[F], HttpRoutes[F])],
    e: Option[(SpanExporter[F, Chunk], HttpRoutes[F])],
    bindHost: String,
    bindPort: Int,
    endpoint: String,
    executionContext: Option[ExecutionContext]
  )(
    builder: BlazeServerBuilder[F] => BlazeServerBuilder[F]
  ): Resource[F, (Option[SpanSampler[F]], Option[SpanCompleter[F]], Option[SpanExporter[F, Chunk]])] = for {
    _ <- HttpResource(
      (s.map(_._2) <+> c.map(_._2) <+> e.map(_._2)).getOrElse(HttpRoutes.empty[F]),
      bindHost,
      bindPort,
      endpoint,
      executionContext
    )(builder)
  } yield (s.map(_._1), c.map(_._1), e.map(_._1))
}
