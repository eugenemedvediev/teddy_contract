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
class RestServiceActor(val db: DB, val servers: scala.collection.mutable.Map[String, APIServer]) extends Actor with RestService {
  implicit def actorRefFactory = context

  def receive = runRoute(rest)
}

/**
  * REST Service
  */
trait RestService extends HttpService with SLF4JLogging {

  val db: DB
  val servers: scala.collection.mutable.Map[String, APIServer]
  private implicit val executionContext = actorRefFactory.dispatcher
  private val fromPortKey = "FROM_PORT"
  private val fromPortValue = "9000"
  private val toPortKey = "TO_PORT"
  private val toPortValue = "9005"
  private val fromPort = sys.env.getOrElse(fromPortKey, fromPortValue).toInt
  private val toPort = sys.env.getOrElse(toPortKey, toPortValue).toInt
  val contracts = "contracts"

  protected val rest =
    path("") {
      getFromResource("index.html")
    } ~
      respondWithMediaType(MediaTypes.`application/json`) {
        path(contracts) {
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
                  val server = DummyCreator.createServer(fromPort, configuration)
                  server.start
                  val port = server.getPort
                  if (port > toPort) {
                    server.stop()
                    ctx.complete(StatusCodes.BadRequest, s"""{"error": "Port limit reached; $fromPort-$toPort"}""")
                  } else {
                    servers.put(port.toString, server)
                    db.writeConfiguration(port.toString, configuration)
                    ctx.complete(StatusCodes.OK, s"""{"port": $port}""")
                  }
                }
            }
          } ~ get {
            ctx: RequestContext => {
              val configurations = db.getAllStartedConfigurations.map(x => (x._1.toString, x._2))
              implicit val formats = DefaultFormats
              ctx.complete(StatusCodes.OK, Serialization.write(configurations))
            }
          }
        } ~
          path(contracts / IntNumber) { port =>
            delete {
              ctx: RequestContext => {
                try {
                  val server = servers(port.toString)
                  servers.remove(port.toString)
                  server.stop()
                } catch {
                  case e: Exception => log.error(s"Attempt to delete not existing server: ${e.getMessage}")
                }

                try {
                  db.deleteConfiguration(port.toString)
                } catch {
                  case e: Exception => log.error(s"Attempt to delete not existing server from db ${e.getMessage}")
                }

                ctx.complete(StatusCodes.NoContent)
              }
            }
          }
      }

}
