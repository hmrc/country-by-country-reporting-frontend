import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.4.0"
  private val hmrcMongoVersion = "1.7.0"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "8.5.0",
    "uk.gov.hmrc" %% "play-conditional-form-mapping-play-30" % "2.0.0",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % hmrcMongoVersion,
    "org.julienrf" %% "play-json-derived-codecs" % "11.0.0",
    "uk.gov.hmrc" %% "crypto-json-play-28" % "7.6.0"

  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus"      %% "scalacheck-1-15"         % "3.2.11.0",
    "org.scalatestplus"      %% "mockito-3-4"             % "3.2.10.0",
    "org.pegdown"             % "pegdown"                 % "1.6.0",
    "org.jsoup"               % "jsoup"                   % "1.17.2",
    "org.mockito"            %% "mockito-scala"           % "1.17.31",
    "wolfendale"             %% "scalacheck-gen-regexp"   % "0.1.2"
  ).map(_ % Test)

  val itDependencies: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
