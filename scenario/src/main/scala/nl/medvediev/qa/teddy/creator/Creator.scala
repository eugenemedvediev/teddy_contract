package nl.medvediev.qa.teddy.creator

import com.github.kxbmap.configs._
import nl.medvediev.qa.teddy.creator.DummyCreator._
import nl.medvediev.qa.teddy.model.{Scenario, Path}
import com.typesafe.config.ConfigFactory
import fr.simply._
import fr.simply.util.ContentType
import org.json4s
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.simpleframework.http.Request

/**
 * Created by ievgen on 30/07/15.
 */
trait Creator {

  var index: Int = 0

  def createServerRoutes(list: List[Path]): List[ServerRoute]

  def reset(): Unit = {
    index = 0
  }

  val DUMMY_CONFIGURATION = "/_contract_/configuration"

  val DEFAULT_WORKING_SERVER_DESCRIPTION = "working server"

  val APPLICATION_JSON: ContentType = fr.simply.util.ContentType("application/json")

  def aPost = POST(_, _, _)

  def aPut = PUT(_, _, _)

  def aPatch = PATCH(_, _, _)

  def aGet = GET(_, _, _)

  def aDelete = DELETE(_, _, _)

  def aOptions = OPTIONS(_, _, _)

  //TODO: add all http methods
  val map = Map(
    "POST" -> aPost,
    "PUT" -> aPut,
    "PATCH" -> aPatch,
    "GET" -> aGet,
    "DELETE" -> aDelete,
    "OPTIONS" -> aOptions
  )

  def jsonFromFile(name: String): JValue = {
    parse(scala.io.Source.fromInputStream(getClass.getClassLoader.getResource(name).openStream()).getLines().mkString)
  }

  def createWorkingServer(port: Int = 8090, description: String, api: List[Path]) = new StubServer(port, (createStubConfigurationServerRoute(description, api) :: createServerRoutes(api)).toArray: _*).defaultResponse(APPLICATION_JSON, """{"contract_error":"not supported path or method by contract; check configuration GET %s"}""".format(DUMMY_CONFIGURATION), 404)

  def createStubConfigurationServerRoute(description: String, api: List[Path]): ServerRoute = {
    GET(
      path = DUMMY_CONFIGURATION,
      response = DynamicServerResponse({ request =>
        implicit val formats = DefaultFormats
        val server: String = compact(
            ("description" -> description) ~
            ("api" -> parse(Serialization.write(api)))
        )
        StaticServerResponse(fr.simply.util.ContentType("application/json"), server, 200)
      })
    )
  }

  def getMethod(method: String) = map.getOrElse(method, throw new IllegalArgumentException(s"Unsupported method: $method"))

  def parseBody(x: JsonAST.JValue): String = {
    implicit lazy val formats = org.json4s.DefaultFormats
    val body: JValue = x \ "body"
    body match {
      case _: JArray => compact(body)
      case _: JObject => compact(body)
      case _: JString => try {
        compact(jsonFromFile(body.extract[String]))
      } catch {
        case _: Throwable => body.extract[String]
      }
      case _ => throw new scala.IllegalArgumentException(s"Not supported body: $body")
    }
  }

  def getHeaders(request: Request): Map[String, String] = {
    val tuples: List[(String, String)] = for {
      name <- request.getNames.toArray.toList
    } yield (name.toString, request.getValue(name.toString))
    tuples.toMap
  }

  def getScenariosWithHeaders(scenarios: List[Scenario], headers: Map[String, String]): List[Scenario] = {
    if (headers.isEmpty) {
      scenarios.filter(scenario =>
        scenario.request.headers.isEmpty
      )
    } else {
      scenarios.filter(scenario =>
        scenario.request.headers.forall(sh =>
          headers.contains(sh._1) && headers.getOrElse(sh._1, null) == sh._2
        )
      )
    }
  }

  def getScenariosWithRequiredHeaders(scenarios: List[Scenario], headers: Map[String, String]): List[Scenario] = {
    val withRequiredHeaders: List[Scenario] = scenarios.filter(scenario =>
      scenario.request.headers.exists(h => h._1.startsWith("!"))
    )
    if (headers.isEmpty) {
      withRequiredHeaders
    } else {
      withRequiredHeaders.filter(scenario =>
        scenario.request.headers.exists(h => !headers.contains(h._1.substring(1)) || (h._2 != null && headers.getOrElse(h._1.substring(1), null) != h._2))
      )
    }
  }


}
