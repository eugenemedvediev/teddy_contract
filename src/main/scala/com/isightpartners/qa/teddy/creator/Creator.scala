package com.isightpartners.qa.teddy.creator

import com.github.kxbmap.configs._
import com.isightpartners.qa.teddy.creator.DummyCreator._
import com.typesafe.config.ConfigFactory
import fr.simply._
import org.json4s
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
 * Created by ievgen on 30/07/15.
 */
trait Creator {

  def generateServerRoute(x: JValue): ServerRoute

  val defaultAPISettingsKey: String

  val STUB_CONFIGURATION = "/stub/configuration"

  val DEFAULT_WORKING_SERVER_DESCRIPTION = "working server"

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

  def loadDefaultWorkingAPI: json4s.JValue = {
    val apiFile: String = ConfigFactory.load().get[String](defaultAPISettingsKey)
    jsonFromFile(apiFile)
  }

  def jsonFromFile(name: String): JValue = {
    parse(scala.io.Source.fromInputStream(getClass.getClassLoader.getResource(name).openStream()).getLines().mkString)
  }

  def createWorkingServer(name: String, description: String, api: JValue) = new StubServer(8090, (createStubConfigurationServerRoute(name, description, api) :: createServerRoutes(api.children)).toArray: _*).defaultResponse(APPLICATION_JSON, """{"contract_error":"not supported path or method by contract; check configuration GET %s"}""".format(STUB_CONFIGURATION), 404)

  def createDefaultServer(name: String) = createWorkingServer(name, DEFAULT_WORKING_SERVER_DESCRIPTION, loadDefaultWorkingAPI)

  def createStubConfigurationServerRoute(name: String, description: String, api: JValue): ServerRoute = {
    GET(
      path = STUB_CONFIGURATION,
      response = DynamicServerResponse({ request =>
        implicit val formats = DefaultFormats
        val server: String = compact(
          ("name" -> name) ~
            ("description" -> description) ~
            ("api" -> Extraction.decompose(api))
        )
        StaticServerResponse(fr.simply.util.ContentType("application/json"), server, 200)
      })
    )
  }

  def createServerRoutes(list: List[JsonAST.JValue]) = {
    def loop(list: List[JsonAST.JValue], acc: List[ServerRoute]): List[ServerRoute] = list match {
      case Nil => acc
      case x :: xs => generateServerRoute(x) :: loop(xs, acc)
    }
    loop(list, List())
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

  def parseHeaders(x: JsonAST.JValue): Map[String, String] = {
    implicit lazy val formats = org.json4s.DefaultFormats
    val body: JValue = x \ "headers"
    body match {
      case _: JObject => body.extract[Map[String, String]]
      case _ => Map[String, String]()
    }
  }

}
