/**
 * iSIGHT Partners, Inc. Proprietary
 */

package com.isightpartners.qa.teddy

import com.github.kxbmap.configs._
import com.isightpartners.qa.teddy.model.Scenario
import com.typesafe.config.ConfigFactory
import fr.simply._
import fr.simply.util.ContentType
import org.json4s
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/3/15
 */
object Creator {

  val METHOD: String = "method"
  val PATH: String = "path"
  val CONTENT_TYPE: String = "contentType"
  val CODE: String = "code"
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

  def loadDefaultWorkingAPI: json4s.JValue = {
    val apiFile: String = ConfigFactory.load().get[String]("working.server.api")
    jsonFromFile(apiFile)
  }

  def createWorkingServer(name: String, description: String, api: JValue) = new StubServer(8090, (GET(
    path = Constants.STUB_CONFIGURATION,
    response = DynamicServerResponse({ request =>
      implicit val formats = DefaultFormats
      val server: String = compact(
        ("name" -> name) ~
          ("description" -> description) ~
          ("api" -> Extraction.decompose(api))
      )
      StaticServerResponse(fr.simply.util.ContentType("application/json"), server, 200)
    })
  ) :: createServerRoutes(api.children)).toArray: _*).defaultResponse(APPLICATION_JSON, """{"contract_error":"not supported path or method by contract; check configuration GET %s"}""".format(Constants.STUB_CONFIGURATION), 404)

  def jsonFromFile(name: String): JValue = {
    parse(scala.io.Source.fromInputStream(getClass.getClassLoader.getResource(name).openStream()).getLines().mkString)
  }

  def createServerRoutes(list: List[JsonAST.JValue]) = {

    def generateServerRouteList(x: JValue): List[ServerRoute] = {
      implicit lazy val formats = org.json4s.DefaultFormats
      if (x \ "scenarios" == JNothing || (x \ "scenarios").extract[List[Scenario]] == null) {
        List[ServerRoute]()
      }
      else {
        val scenarios = (x \ "scenarios").extract[List[Scenario]]
        val method = (x \ METHOD).extract[String]
        val path = (x \ PATH).extract[String]
        for {scenario <- scenarios} yield createServerRoute(method, path, scenario)
      }
    }

    def createServerRoute(method: String, path: String, scenario: Scenario): ServerRoute = {
      getMethod(method)(
        path + {
          if (scenario.name == null || scenario.name.isEmpty || scenario.name.equals("ok")) "" else s"_${scenario.name}"
        },
        Map(),
        DynamicServerResponse({ request =>
          if (scenario.body == null)
            StaticServerResponse(APPLICATION_JSON, """{"contract_error":"body not provided by contract"}""", 503)
          else {
            implicit lazy val formats = org.json4s.DefaultFormats
            StaticServerResponse(fr.simply.util.ContentType(scenario.contentType), Serialization.write(scenario.body), scenario.code, scenario.headers)
          }
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

    def parseHeaders(x: JsonAST.JValue): Map[String, String] = {
      implicit lazy val formats = org.json4s.DefaultFormats
      val body: JValue = x \ "headers"
      body match {
        case _: JObject => body.extract[Map[String, String]]
        case _ => Map[String, String]()
      }
    }

    def loop(list: List[JsonAST.JValue], acc: List[ServerRoute]): List[ServerRoute] = list match {
      case Nil => acc
      case x :: xs => generateServerRouteList(x) ::: loop(xs, acc)
    }
    loop(list, List())
  }


}
