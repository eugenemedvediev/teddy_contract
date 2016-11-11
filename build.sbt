import com.github.retronym.SbtOneJar._
import sbt._
import sbt.Keys._

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "Big Bee Consultants" at "http://bigbeeconsultants.co.uk/repo"

val json4s = "org.json4s" %% "json4s-jackson" % "3.2.11"
val configs = "com.github.kxbmap" %% "configs" % "0.2.2"
val apacheClient = "org.apache.httpcomponents" % "httpclient" % "4.3.6"
val javaServlet = "javax.servlet" % "servlet-api" % "2.5" % "provided"
val scalatest = "org.scalatest" % "scalatest_2.10" % "2.1.3" % "test"
val beeclientit = "uk.co.bigbeeconsultants" %% "bee-client" % "0.28.0" % "it"
val slf4j = "org.slf4j" % "slf4j-api" % "1.7.12"
val logbackcore = "ch.qos.logback" % "logback-core" % "1.1.3"
val logbackclassic = "ch.qos.logback" % "logback-classic" % "1.1.3"

val itTestFilter: String => Boolean = { name =>
  (name endsWith "ItTest") || (name endsWith "IntegrationTest")
}

lazy val commonSettings = Seq(
  organization := "com.github.eugenemedvediev",
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

lazy val scenario = (project in file("scenario")).
  settings(commonSettings: _*).
  settings(
    name := "scenario",
    libraryDependencies ++= Seq(
      configs,
      apacheClient,
      javaServlet,
      "org.scalatest" % "scalatest_2.10" % "2.1.3" % "test,it"
    ),
    Defaults.itSettings,
    testOptions in IntegrationTest += Tests.Filter(itTestFilter),
    parallelExecution in IntegrationTest := true,
    oneJarSettings,
    mainClass in oneJar := Some("qa.scenario.ScenarioServer")
  ).
  configs(IntegrationTest).
  dependsOn(common, http)

lazy val dummy = (project in file("dummy")).
  settings(commonSettings: _*).
  enablePlugins(DockerPlugin).
  settings(
    name := "dummy",
    libraryDependencies ++= Seq(
      "uk.co.bigbeeconsultants" %% "bee-client" % "0.28.0" % "it" excludeAll(ExclusionRule(organization = "org.scalatest"), ExclusionRule(organization = "javax.servlet")),
      "org.scalatest" % "scalatest_2.10" % "2.1.3" % "test,it"
    ),
    Defaults.itSettings,
    testOptions in IntegrationTest += Tests.Filter(itTestFilter),
    parallelExecution in IntegrationTest := true,
    oneJarSettings,
    mainClass in oneJar := Some("qa.dummy.DummyServer"),
    dockerfile in docker := {
      val jarFile = (artifactPath in oneJar).value
      val appDirPath = "/app"
      val jarTargetPath = s"$appDirPath/dummy.jar"
      val configTargetPath = s"$appDirPath/config.json"
      print(s"classpath: ${(resourceDirectory in Compile).value / "test.json"}")
      new Dockerfile {
        from("java")
        add(jarFile, jarTargetPath)
        add((resourceDirectory in Compile).value / "test.json", configTargetPath)
        expose(8090)
        entryPoint("java", "-jar", jarTargetPath, configTargetPath)
      }
    }
  ).
  configs(IntegrationTest).
  dependsOn(common, http)

lazy val teddy_contract = (project in file(".")).
  settings(
    name := "teddy_contract"
  ).
  aggregate(dummy)
//  aggregate(dummy, scenario)
