resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

val simplyscala = "com.github.simplyscala" %% "simplyscala-server" % "0.5"
val json4s = "org.json4s" %% "json4s-jackson" % "3.2.11"
val configs = "com.github.kxbmap" %% "configs" % "0.2.2"
val apacheClient = "org.apache.httpcomponents" % "httpclient" % "4.3.6"
val javaServlet = "javax.servlet" % "servlet-api" % "2.5" % "provided"
val scalatest = "org.scalatest" % "scalatest_2.10" % "2.0" % "test"
val scalatestit = "org.scalatest" % "scalatest_2.10" % "2.0" % "it"
val elasticsearch = "com.sksamuel.elastic4s" %% "elastic4s" % "1.1.2.0"

lazy val commonSettings = Seq(
  organization := "com.isightpartners",
  version := "0.0.1",
  scalaVersion := "2.10.4"
)

lazy val teddy_contract = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "teddy_contract",
    libraryDependencies ++= Seq(simplyscala, json4s, configs, apacheClient, javaServlet, scalatest, scalatestit, elasticsearch)
  ).configs(IntegrationTest)

jetty()

Defaults.itSettings

val itTestFilter: String => Boolean = { name =>
  (name endsWith "ItTest") || (name endsWith "IntegrationTest")
}

testOptions in IntegrationTest += Tests.Filter(itTestFilter)

mainClass := Some("com.isightpartners.qa.teddy.util.StubLoader")

parallelExecution in IntegrationTest := true