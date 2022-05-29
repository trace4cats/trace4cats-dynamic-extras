package trace4cats.dynamic.http4s

import io.circe.Json
import io.circe.syntax._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import trace4cats.dynamic.config.SamplerConfig
import trace4cats.dynamic.http4s.SamplerConfigCodec._

class SamplerConfigCodecSpec extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  implicit val samplerConfigArbitrary: Arbitrary[SamplerConfig] = Arbitrary(
    Gen.oneOf(
      Gen.const(SamplerConfig.Always),
      Gen.const(SamplerConfig.Never),
      Gen.zip(Gen.double, Gen.prob(0.5)).map((SamplerConfig.Probabilistic.apply _).tupled),
      Gen.zip(Gen.choose(1, 10), Gen.double, Gen.prob(0.5)).map((SamplerConfig.Rate.apply _).tupled)
    )
  )

  behavior.of("SamplerConfigCodec")

  it should "encode and decode consistently" in forAll { (cfg: SamplerConfig) =>
    val result = cfg.asJson.as[SamplerConfig]
    result shouldBe Right(cfg)
  }

  it should "respect defaults in Probabilistic constructor" in {
    val cfg: SamplerConfig = SamplerConfig.Probabilistic(0.1)
    val result = Json
      .fromJsonObject(cfg.asJsonObject.remove("rootSpansOnly"))
      .as[SamplerConfig]
      .toOption
      .collect { case c: SamplerConfig.Probabilistic =>
        c.rootSpansOnly
      }
    result shouldBe Some(true)
  }

  it should "respect defaults in Rate constructor" in {
    val cfg: SamplerConfig = SamplerConfig.Rate(10, 0.1)
    val result = Json
      .fromJsonObject(cfg.asJsonObject.remove("rootSpansOnly"))
      .as[SamplerConfig]
      .toOption
      .collect { case c: SamplerConfig.Rate =>
        c.rootSpansOnly
      }
    result shouldBe Some(true)
  }

  it should "encode ADT sums with discriminator" in forAll { (cfg: SamplerConfig) =>
    val result = cfg.asJsonObject.contains("samplerType")
    result shouldBe true
  }

}
