package io.janstenpickle.trace4cats.sampling.dynamic.http

import cats.effect.kernel.Resource

case class HotSwapConstructor[F[_], A, R](initialConfig: A, make: A => Resource[F, R])

object HotSwapConstructor {
  def apply[F[_], A, R](initialConfig: A)(make: A => Resource[F, R]): HotSwapConstructor[F, A, R] =
    HotSwapConstructor(initialConfig, make)
}
