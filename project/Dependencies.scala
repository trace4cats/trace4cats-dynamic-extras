import sbt._

object Dependencies {
  object Versions {
    val scala212 = "2.12.14"
    val scala213 = "2.13.6"

    val trace4cats = "0.12.0-RC1+162-70070fb2"

    val cats = "2.6.1"
    val catsEffect = "3.1.1"
    val circe = "0.14.1"
    val http4s = "0.23.0-RC1"

    val catsTestkitScalatest = "2.1.5"
    val disciplineScalatest = "2.1.5"
    val discipline = "1.1.5"
    val scalaCheck = "1.15.4"
    val scalaCheckShapeless = "1.3.0"
    val scalaTest = "3.2.9"
  }

  lazy val trace4catsDynamicSamplingConfig =
    "io.janstenpickle"                           %% "trace4cats-dynamic-sampling-config" % Versions.trace4cats
  lazy val trace4catsKernel = "io.janstenpickle" %% "trace4cats-kernel"                  % Versions.trace4cats
  lazy val trace4catsModel = "io.janstenpickle"  %% "trace4cats-model"                   % Versions.trace4cats

  lazy val circeGeneric = "io.circe"        %% "circe-generic-extras" % Versions.circe
  lazy val http4sCirce = "org.http4s"       %% "http4s-circe"         % Versions.http4s
  lazy val http4sCore = "org.http4s"        %% "http4s-core"          % Versions.http4s
  lazy val http4sDsl = "org.http4s"         %% "http4s-dsl"           % Versions.http4s
  lazy val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server"  % Versions.http4s
  lazy val http4sServer = "org.http4s"      %% "http4s-server"        % Versions.http4s

  lazy val catsLaws = "org.typelevel"             %% "cats-laws"              % Versions.cats
  lazy val catsEffectLaws = "org.typelevel"       %% "cats-effect-laws"       % Versions.catsEffect
  lazy val catsEffectTestkit = "org.typelevel"    %% "cats-effect-testkit"    % Versions.catsEffect
  lazy val catsTestkitScalatest = "org.typelevel" %% "cats-testkit-scalatest" % Versions.catsTestkitScalatest
  lazy val disciplineScalatest = "org.typelevel"  %% "discipline-scalatest"   % Versions.disciplineScalatest
  lazy val disciplineCore = "org.typelevel"       %% "discipline-core"        % Versions.discipline
  lazy val scalacheck = "org.scalacheck"          %% "scalacheck"             % Versions.scalaCheck
  lazy val scalacheckShapeless =
    "com.github.alexarchambault"       %% "scalacheck-shapeless_1.15" % Versions.scalaCheckShapeless
  lazy val scalaTest = "org.scalatest" %% "scalatest"                 % Versions.scalaTest

  lazy val test =
    Seq(
      catsLaws,
      catsEffectLaws,
      catsEffectTestkit,
      catsTestkitScalatest,
      disciplineScalatest,
      disciplineCore,
      scalacheck,
      scalacheckShapeless,
      scalaTest
    )
}
