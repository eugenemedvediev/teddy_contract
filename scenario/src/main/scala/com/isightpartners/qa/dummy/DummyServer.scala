package com.isightpartners.qa.dummy

import com.isightpartners.qa.teddy.model.Configuration
import fr.simply.StubServer
import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
 * Created by ievgen on 09/09/15.
 */
object DummyServer {

  def main(args: Array[String]): Unit = {
    if (args.length >= 2) {
      val json: JValue = jsonFromFile(args(0))
      val initialPort = args(1).toInt
      implicit val formats = DefaultFormats
      val configuration = json.extract[Configuration]
      val workingServer: StubServer = DummyCreator.createServer(initialPort, configuration.description, configuration.api)
      workingServer.start
      val port = workingServer.portInUse
      println(s"Server:\n\thttp://localhost:$port")
      printConfigurationPaths(configuration)
      println(s"Full configuration:\n\tcurl -XGET http://localhost:$port${DummyCreator.DUMMY_CONFIGURATION}")
    } else {
      println("Configuration missing")
    }
  }

  def printConfigurationPaths(configuration: Configuration): Unit = {
    println("Resources:")
    val map = configuration.api.map(p => (p.path, p.method)).groupBy(p => p._1)
    val stringToStrings = map.map(p => (p._1, p._2.map(k => k._2))).toList.sortBy(_._1)
    stringToStrings.foreach(p => println( s"""\t${p._1} - ${p._2.mkString(", ")}"""))
  }

  def jsonFromFile(name: String): JValue = {
    parse(scala.io.Source.fromFile(name).getLines().mkString)
  }

}
