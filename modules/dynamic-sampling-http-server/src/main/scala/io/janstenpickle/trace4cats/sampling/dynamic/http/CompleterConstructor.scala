package io.janstenpickle.trace4cats.sampling.dynamic.http

import cats.effect.kernel.Resource
import io.janstenpickle.trace4cats.kernel.{SpanCompleter, SpanExporter}

case class CompleterConstructor[F[_], G[_], A](
  initialConfig: A,
  make: (A, SpanExporter[F, G]) => Resource[F, SpanCompleter[F]]
)

object CompleterConstructor {
  def apply[F[_], G[_], A](initialConfig: A)(
    make: (A, SpanExporter[F, G]) => Resource[F, SpanCompleter[F]]
  ): CompleterConstructor[F, G, A] =
    CompleterConstructor(initialConfig, make)
}
