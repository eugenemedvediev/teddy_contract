import sbt.Keys._
import sbt._

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "Big Bee Consultants" at "http://bigbeeconsultants.co.uk/repo"

val json4s = "org.json4s" %% "json4s-jackson" % "3.2.11"
val configs = "com.github.kxbmap" %% "configs" % "0.2.2"
val apacheClient = "org.apache.httpcomponents" % "httpclient" % "4.3.6"
val javaServlet = "javax.boot" % "boot-api" % "2.5" % "provided"
val scalatest = "org.scalatest" % "scalatest_2.10" % "2.1.3" % "test"
val beeclientit = "uk.co.bigbeeconsultants" %% "bee-client" % "0.28.0" % "it"
val slf4j = "org.slf4j" % "slf4j-api" % "1.7.12"
val logbackcore = "ch.qos.logback" % "logback-core" % "1.1.3"
val logbackclassic = "ch.qos.logback" % "logback-classic" % "1.1.3"
val elasticsearch = "com.sksamuel.elastic4s" %% "elastic4s" % "1.5.14"

val itTestFilter: String => Boolean = { name =>
  (name endsWith "ItTest") || (name endsWith "IntegrationTest")
}

lazy val commonSettings = Seq(
  organization := "nl.medvediev",
  version := "0.0.1",
  scalaVersion := "2.10.5"
)

lazy val apiserver = (project in file("apiserver")).
  settings(
    libraryDependencies ++= Seq(
      "org.simpleframework" % "simple-http" % "6.0.1",
      "org.simpleframework" % "simple-transport" % "6.0.1",
      "org.scalatest" %% "scalatest" % "2.2.6" % "test",
      "com.typesafe.play" % "play-ws_2.10" % "2.4.6" % "test"
    ),
    exportJars := true
  )

lazy val http = (project in file("http")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= Seq(
    ),
    exportJars := true
  ).
  dependsOn(apiserver)

lazy val common = (project in file("common")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= Seq(
      json4s
    ),
    exportJars := true
  )

//lazy val scenario = (project in file("scenario")).
//  settings(commonSettings: _*).
//  settings(
//    name := "scenario",
//    libraryDependencies ++= Seq(
//      configs,
//      apacheClient,
//      javaServlet,
//      "org.scalatest" % "scalatest_2.10" % "2.1.3" % "test,it"
//    ),
//    Defaults.itSettings,
//    testOptions in IntegrationTest += Tests.Filter(itTestFilter),
//    parallelExecution in IntegrationTest := true,
//    oneJarSettings,
//    mainClass in oneJar := Some("qa.scenario.ScenarioServer")
//  ).
//  configs(IntegrationTest).
//  dependsOn(common, http)

lazy val dummy = (project in file("dummy")).
  settings(commonSettings: _*).
  enablePlugins(JavaAppPackaging).
  settings(
    name := "api-contract",
    version := "0.0.1",
    libraryDependencies ++= Seq(
      elasticsearch,
      "uk.co.bigbeeconsultants" %% "bee-client" % "0.28.0" % "it" excludeAll(ExclusionRule(organization = "org.scalatest"), ExclusionRule(organization = "javax.boot")),
      "org.scalatest" % "scalatest_2.10" % "2.1.3" % "test,it",
      "io.spray" % "spray-can" % "1.1-M8",
      "io.spray" % "spray-http" % "1.1-M8",
      "io.spray" % "spray-routing" % "1.1-M8",
      "net.liftweb" %% "lift-json" % "2.5.1",
      "com.typesafe.slick" %% "slick" % "1.0.1",
      "mysql" % "mysql-connector-java" % "5.1.25",
      "com.typesafe.akka" %% "akka-actor" % "2.1.4",
      "com.typesafe.akka" %% "akka-slf4j" % "2.1.4",
      "ch.qos.logback" % "logback-classic" % "1.0.13"
    ),
    resolvers ++= Seq(
      "Spray repository" at "http://repo.spray.io",
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    Defaults.itSettings,
    testOptions in IntegrationTest += Tests.Filter(itTestFilter),
    parallelExecution in IntegrationTest := true,
    Revolver.settings,
    mainClass in Compile := Some("qa.dummy.boot.Main"),
    packageName in Docker := packageName.value,
    dockerRepository := Some("imedvediev")
  ).
  configs(IntegrationTest).
  dependsOn(common, http)

lazy val teddy_contract = (project in file(".")).
  settings(
    name := "teddy_contract"
  ).
  dependsOn(dummy).aggregate(dummy)
//.dependsOn(scenario).aggregate(scenario)
