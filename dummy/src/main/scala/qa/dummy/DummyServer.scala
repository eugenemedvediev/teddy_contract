package qa.dummy

import fr.simply.StubServer
import org.json4s._
import org.json4s.jackson.JsonMethods._
import qa.common.model.Configuration
import qa.common.Util

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
      val workingServer: StubServer = DummyCreator.createServer(initialPort, configuration)
      workingServer.start
      val port = workingServer.portInUse
      println(s"Server:\n\thttp://localhost:$port")
      printConfigurationResources(configuration)
      println(s"Full configuration:\n\tcurl -XGET http://localhost:$port${DummyCreator.DUMMY_CONFIGURATION}")
    } else {
      println("Configuration missing")
    }
  }

  def printConfigurationResources(configuration: Configuration): Unit = {
    println("Resources:")
    val list = Util.getStringifiedRoutes(configuration)
    list.foreach(r => println("\t" + r))
  }

  def getResourcesFromConfiguration(configuration: Configuration): List[String] = {
    val groupedByPath = configuration.api.map(r => (r.path, r.method)).groupBy(_._1)
    val sortedByPath = groupedByPath.map(p => (p._1, p._2.map(k => k._2))).toList.sortBy(_._1)
    val strings = sortedByPath.map(p => s"""${p._1} - ${p._2.mkString(", ")}""")
    strings
  }

  def jsonFromFile(name: String): JValue = {
    parse(scala.io.Source.fromFile(name).getLines().mkString)
  }

}
