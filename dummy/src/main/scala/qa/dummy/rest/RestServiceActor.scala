package qa.dummy.rest


import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import java.text.{ParseException, SimpleDateFormat}
import java.util.Date

import net.liftweb.json.Serialization._
import net.liftweb.json.{DateFormat, Formats}
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

  implicit val liftJsonFormats = new Formats {
    val dateFormat = new DateFormat {
      val sdf = new SimpleDateFormat("yyyy-MM-dd")

      def parse(s: String): Option[Date] = try {
        Some(sdf.parse(s))
      } catch {
        case e: Exception => None
      }

      def format(d: Date): String = sdf.format(d)
    }
  }

  implicit val string2Date = new FromStringDeserializer[Date] {
    def apply(value: String) = {
      val sdf = new SimpleDateFormat("yyyy-MM-dd")
      try Right(sdf.parse(value))
      catch {
        case e: ParseException => {
          Left(MalformedContent("'%s' is not a valid Date value" format (value), e))
        }
      }
    }
  }

  implicit val customRejectionHandler = RejectionHandler {
    case rejections => mapHttpResponse {
      response =>
        response.withEntity(HttpEntity(ContentType(MediaTypes.`application/json`),
          write(Map("error" -> response.entity.asString))))
    } {
      RejectionHandler.Default(rejections)
    }
  }

  val rest = respondWithMediaType(MediaTypes.`application/json`) {
    path("servers") {
      post {
        entity(Unmarshaller(MediaTypes.`application/json`) {
          case httpEntity: HttpEntity =>
            read[Configuration](httpEntity.asString(HttpCharsets.`UTF-8`))
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
