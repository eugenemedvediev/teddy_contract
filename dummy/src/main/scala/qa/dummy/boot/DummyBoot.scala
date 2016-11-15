package qa.dummy.boot

import akka.actor.{ActorSystem, Props}
import akka.event.slf4j.SLF4JLogging
import akka.io.IO
import qa.dummy.config.AppConfiguration
import qa.dummy.rest.RestServiceActor
import spray.can.Http

/**
  * Created by ievgen on 11/11/2016.
  */
object DummyBoot extends App with AppConfiguration  with SLF4JLogging {
    // create an actor system for application
    log.info("YO")
    implicit val system = ActorSystem("rest-service-example")

    // create and start rest service actor
    val restService = system.actorOf(Props[RestServiceActor], "servers")

    // start HTTP server with rest service actor as a handler
    IO(Http) ! Http.Bind(restService, "localhost", 8080)

}
