package qa.dummy.boot

import akka.actor.{ActorSystem, Props}
import akka.event.slf4j.SLF4JLogging
import akka.io.IO
import qa.dummy.config.AppConfiguration
import qa.dummy.rest.RestServiceActor
import spray.can.Http

object Main extends App with AppConfiguration  with SLF4JLogging {
    implicit val system = ActorSystem("servers")
    val restService = system.actorOf(Props[RestServiceActor], "servers")
    IO(Http) ! Http.Bind(restService, interface, port)
}
