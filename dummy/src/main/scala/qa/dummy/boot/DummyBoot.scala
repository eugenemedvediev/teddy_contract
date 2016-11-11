package qa.dummy.boot

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import qa.dummy.config.AppConfiguration
import qa.dummy.rest.RestServiceActor
import spray.can.Http

/**
  * Created by ievgen on 11/11/2016.
  */
class DummyBoot extends App with AppConfiguration {
    // create an actor system for application
    println("YO")
    implicit val system = ActorSystem("rest-service-example")

    // create and start rest service actor
    val restService = system.actorOf(Props[RestServiceActor], "servers")

    // start HTTP server with rest service actor as a handler
    IO(Http) ! Http.Bind(restService, "localhost", 8080)

}
