package io.janstenpickle.trace4cats.sampling.dynamic.http

import cats.effect.kernel.Resource
import io.janstenpickle.trace4cats.kernel.SpanExporter

case class ExporterConstructor[F[_], G[_], A](initialConfig: A, make: A => Resource[F, SpanExporter[F, G]])

object ExporterConstructor {
  def apply[F[_], G[_], A](initialConfig: A)(make: A => Resource[F, SpanExporter[F, G]]): ExporterConstructor[F, G, A] =
    ExporterConstructor(initialConfig, make)
}
