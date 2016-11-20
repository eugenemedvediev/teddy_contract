package qa.dummy.boot

import akka.actor.{ActorSystem, Props}
import akka.event.slf4j.SLF4JLogging
import akka.io.IO
import com.typesafe.config.ConfigFactory
import nl.medvediev.apiserver.APIServer
import qa.common.model.Configuration
import qa.dummy.DummyCreator
import qa.dummy.config.AppConfiguration
import qa.dummy.db.ESDB
import qa.dummy.rest.RestServiceActor
import spray.can.Http

import scala.collection.mutable

object Main extends App with AppConfiguration with SLF4JLogging {

  val servers = mutable.Map[String, APIServer]()
  private val db = new ESDB(elastic_home = ConfigFactory.load.getString("elastic.home"))
  private val configurations: List[(String, Configuration)] = db.getAllStartedConfigurations
  configurations.foreach {
    case (name: String, configuration: Configuration) => {
      val server = DummyCreator.createServer(name.toInt, configuration)
      server.start
      servers.put(server.getPort.toString, server)
    }
  }

  implicit val system = ActorSystem("servers")
  val restService = system.actorOf(Props(new RestServiceActor(dbP = db, serversP = servers)), "servers")

  IO(Http) ! Http.Bind(restService, interface, port)
}
