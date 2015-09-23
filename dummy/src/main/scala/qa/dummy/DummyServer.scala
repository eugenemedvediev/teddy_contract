package qa.dummy

import qa.common.Util
import qa.common.model.Configuration

object DummyServer {

  var initialPort = 8090

  def main(args: Array[String]): Unit = {
    if (args.length == 0) {
      println("Error: Configuration file is not specified")
      println("Format: java -jar <jarfile> <path to config> <optional: port>")
      sys.exit(1)
    }
    if (args.length >= 2) {
      initialPort = args(1).toInt
    }
    val configuration = Util.extractConfiguration(file = args(0))
    val workingServer = DummyCreator.createServer(initialPort, configuration)
    workingServer.start
    val port = workingServer.portInUse

    println(s"Server:\n\thttp://localhost:$port")
    printConfigurationResources(configuration)
    println(s"Full configuration:\n\tcurl -XGET http://localhost:$port${DummyCreator.DUMMY_CONFIGURATION}")
  }

  def printConfigurationResources(configuration: Configuration): Unit = {
    println("Resources:")
    val list = Util.getStringifiedRoutes(configuration)
    list.foreach(r => println("\t" + r))
  }

}
