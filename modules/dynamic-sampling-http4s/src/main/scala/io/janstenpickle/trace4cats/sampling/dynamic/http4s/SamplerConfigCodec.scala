package io.janstenpickle.trace4cats.sampling.dynamic.http4s

import cats.syntax.either._
import io.circe.{Codec, Decoder, DecodingFailure, Encoder, Json, JsonObject}
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.janstenpickle.trace4cats.sampling.dynamic.config.SamplerConfig

object SamplerConfigCodec {
  // TODO: replace the custom codec with the configured semiauto instance when `circe-generic-extras` is ported to Scala 3
  // see https://github.com/circe/circe-generic-extras/issues/168

  // implicit val circeConfig: Configuration = Configuration.default.withDefaults.withDiscriminator("samplerType")
  // implicit val samplerConfigCodec: Codec.AsObject[SamplerConfig] = deriveConfiguredCodec

  implicit val samplerConfigCodec: Codec.AsObject[SamplerConfig] = {
    val disc = "samplerType"
    implicit val probabilisticCodec: Codec.AsObject[SamplerConfig.Probabilistic] = deriveCodec
    implicit val rateCodec: Codec.AsObject[SamplerConfig.Rate] = deriveCodec

    Codec.AsObject.from(
      Decoder.instance { cur =>
        cur.get[String](disc).flatMap {
          case "Always" => SamplerConfig.Always.asRight
          case "Never" => SamplerConfig.Never.asRight
          case "Probabilistic" =>
            val defaults = Json.obj("rootSpansOnly" := true)
            cur.withFocus(defaults.deepMerge).as[SamplerConfig.Probabilistic]
          case "Rate" =>
            val defaults = Json.obj("rootSpansOnly" := true)
            cur.withFocus(defaults.deepMerge).as[SamplerConfig.Rate]
          case other => Left(DecodingFailure(s"Unknown $disc = '$other'", cur.history))
        }
      },
      Encoder.AsObject.instance {
        case SamplerConfig.Always => JsonObject(disc := "Always")
        case SamplerConfig.Never => JsonObject(disc := "Never")
        case x: SamplerConfig.Probabilistic =>
          (disc := "Probabilistic") +: x.asJsonObject
        case x: SamplerConfig.Rate =>
          (disc := "Rate") +: x.asJsonObject
      }
    )
  }
}
