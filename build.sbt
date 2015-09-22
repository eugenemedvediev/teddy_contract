resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
import com.github.retronym.SbtOneJar._

val simplyscala = "com.github.simplyscala" %% "simplyscala-server" % "0.5"
val json4s = "org.json4s" %% "json4s-jackson" % "3.2.11"
val configs = "com.github.kxbmap" %% "configs" % "0.2.2"
val apacheClient = "org.apache.httpcomponents" % "httpclient" % "4.3.6"
val javaServlet = "javax.servlet" % "servlet-api" % "2.5" % "provided"
val scalatest = "org.scalatest" % "scalatest_2.10" % "2.0" % "test"
val scalatestit = "org.scalatest" % "scalatest_2.10" % "2.0" % "it"
//val elasticsearch = "com.sksamuel.elastic4s" %% "elastic4s" % "1.1.2.0"

lazy val commonSettings = Seq(
  organization := "com.isightpartners",
  version := "0.0.1",
  scalaVersion := "2.10.4"
)

lazy val common = (project in file("common")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= Seq(simplyscala, json4s, configs, apacheClient, javaServlet, scalatest),
    oneJarSettings,
    exportJars := true
  )

lazy val scenario = (project in file("scenario")).
  settings(commonSettings: _*).
  settings(
    oneJarSettings,
    mainClass in oneJar := Some("qa.scenario.ScenarioServer")
  ).
  dependsOn(common)

val itTestFilter: String => Boolean = { name =>
  (name endsWith "ItTest") || (name endsWith "IntegrationTest")
}

lazy val dummy = (project in file("dummy")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= Seq(simplyscala, json4s, configs, apacheClient, javaServlet, scalatest, scalatestit),
    oneJarSettings,
    mainClass in oneJar := Some("qa.dummy.DummyServer"),
    testOptions in IntegrationTest += Tests.Filter(itTestFilter),
    parallelExecution in IntegrationTest := true
  ).
  configs(IntegrationTest).
  dependsOn(common)

lazy val teddy_contract = (project in file(".")).
  settings(
    name := "teddy_contract",
    libraryDependencies ++= Seq(simplyscala, json4s, configs, apacheClient, javaServlet, scalatest)
  ).
  aggregate(dummy, scenario)

jetty()

Defaults.itSettings

//val itTestFilter: String => Boolean = { name =>
//  (name endsWith "ItTest") || (name endsWith "IntegrationTest")
//}

//testOptions in IntegrationTest += Tests.Filter(itTestFilter)

//mainClass := Some("com.isightpartners.qa.dummy.DummyServer")

//parallelExecution in IntegrationTest := true



//oneJarSettings