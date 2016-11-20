package qa.dummy.rest


import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import nl.medvediev.apiserver.APIServer
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import qa.common.model.Configuration
import qa.dummy.DummyCreator
import qa.dummy.db.DB
import spray.http._
import spray.httpx.unmarshalling._
import spray.routing._

/**
  * Created by ievgen on 11/11/2016.
  */
class RestServiceActor(dbP: DB, serversP: scala.collection.mutable.Map[String, APIServer]) extends Actor with RestService {
  val db: DB = dbP
  val servers: scala.collection.mutable.Map[String, APIServer] = serversP

  implicit def actorRefFactory = context

  def receive = runRoute(rest)
}

/**
  * REST Service
  */
trait RestService extends HttpService with SLF4JLogging {

  val db: DB
  val servers: scala.collection.mutable.Map[String, APIServer]
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
              val server = DummyCreator.createServer(8090, configuration)
              server.start
              val port = server.getPort.toString
              servers.put(port, server)
              db.writeConfiguration(port, configuration)
              ctx.complete(StatusCodes.OK, port)
            }
        }
      } ~ get {
        ctx: RequestContext => {
          val configurations = db.getAllStartedConfigurations
          implicit val formats = DefaultFormats
          ctx.complete(StatusCodes.OK, Serialization.write(configurations))
        }
      }
    } ~
      path(IntNumber) { int =>
        delete {
          ctx: RequestContext => {
            val server = servers.get(int.toString).get
            server.stop()
            servers.remove(int.toString)
            db.deleteConfiguration(int.toString)
            ctx.complete(StatusCodes.NoContent)
          }
        }
      }
  }

}
