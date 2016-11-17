package qa.dummy.rest


import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import java.text.{ParseException, SimpleDateFormat}
import java.util.Date

import net.liftweb.json.Serialization._
import net.liftweb.json.{DateFormat, Formats}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import qa.common.model.Configuration
import qa.dummy.DummyCreator
import spray.http._
import spray.httpx.unmarshalling._
import spray.routing._

/**
  * Created by ievgen on 11/11/2016.
  */
class RestServiceActor extends Actor with RestService {

  implicit def actorRefFactory = context

  def receive = runRoute(rest)
}

/**
  * REST Service
  */
trait RestService extends HttpService with SLF4JLogging {

  implicit val executionContext = actorRefFactory.dispatcher

  val rest = respondWithMediaType(MediaTypes.`application/json`) {
    path("") {
      post {
        entity(Unmarshaller(MediaTypes.`application/json`) {
          case httpEntity: HttpEntity => {
            val json: JValue = parse(httpEntity.asString(HttpCharsets.`UTF-8`))
            implicit val formats = DefaultFormats
            json.extract[Configuration]
          }
        }) {
          configuration: Configuration =>
            ctx: RequestContext => {
              val workingServer = DummyCreator.createServer(8090, configuration)
              workingServer.start
              val port = workingServer.getPort
              ctx.complete(StatusCodes.OK, port.toString)
          }
        }
      }
    }
  }

}
