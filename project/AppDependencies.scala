import sbt._

object AppDependencies {
  import play.core.PlayVersion

  private val bootstrapVersion = "7.13.0"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc"            % "6.2.0-play-28",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping" % "1.12.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"    % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"            % "0.74.0",
    "com.typesafe.play" %% "play-json-joda"                % "2.7.4",
    "org.julienrf"      %% "play-json-derived-codecs"      % "10.0.2"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapVersion,
    "org.scalatestplus"      %% "scalacheck-1-15"         % "3.2.10.0",
    "org.scalatestplus"      %% "mockito-3-4"             % "3.2.10.0",
    "com.vladsch.flexmark"    % "flexmark-all"            % "0.64.0",
    "org.pegdown"             % "pegdown"                 % "1.6.0",
    "org.jsoup"               % "jsoup"                   % "1.14.3",
    "com.typesafe.play"      %% "play-test"               % PlayVersion.current,
    "org.mockito"            %% "mockito-scala"           % "1.16.42",
    "com.github.tomakehurst"  % "wiremock-jre8"           % "2.26.0",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % "0.74.0",
    "wolfendale"             %% "scalacheck-gen-regexp"   % "0.1.2"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
