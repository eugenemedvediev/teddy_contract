logLevel := Level.Warn
resolvers ++= Seq(
  "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases/"
)

//addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "2.0.4")
addSbtPlugin("org.scala-sbt.plugins" % "sbt-onejar" % "0.8")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.4.0")
addSbtPlugin("io.spray" %% "sbt-revolver" % "0.8.0")
addSbtPlugin("com.typesafe.sbt" %% "sbt-native-packager" % "1.2.0-M7")