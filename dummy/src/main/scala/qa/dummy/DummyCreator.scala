package qa.dummy

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/3/15
 */

import fr.simply._
import fr.simply.util.ContentType
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.simpleframework.http.Request
import qa.common.http.Methods
import qa.common.model.{Configuration, Route, Scenario, ScenarioResponse}
import qa.common.Util
object DummyCreator {

  val DUMMY_CONFIGURATION = "/_dummy_"

  val DEFAULT_WORKING_SERVER_DESCRIPTION = "working server"

  val APPLICATION_JSON: ContentType = fr.simply.util.ContentType("application/json")

  val EXPECTED_HEADER = "@expectedHeader"

  def createServer(port: Int, configuration: Configuration) = new StubServer(port, (createConfigurationRoute(configuration) :: createServerRoutes(configuration.api)).toArray: _*).defaultResponse(APPLICATION_JSON, """{"contract_error":"not supported path or method by contract; check configuration GET %s"}""".format(DUMMY_CONFIGURATION), 404)

  def createConfigurationRoute(configuration: Configuration): ServerRoute = {
    def generateConfigurationPageContent(configuration: Configuration): String = {
      implicit val formats = DefaultFormats
      val list = Util.getStringifiedRoutes(configuration)
      val htmlList = list.foldLeft[String]("")( (string, elem) => s"$string\n<li>$elem</li>" )
      val html: String =
        """
          |<!DOCTYPE html>
          |<html>
          |<head>
          |<title>Dummy Server</title>
          |</head>
          |<body>
          |<h1>%s</h1>
          |<h3>Routes:<h3>
          |<ul>%s</ul>
          |<h3>Configuration:</h3>
          |<pre><code>%s</code></pre>
          |
          |</body>
          |</html>
        """.format(configuration.description, htmlList, pretty(parse(Serialization.write(configuration.api)))).stripMargin
      html
    }

    GET(
      path = DUMMY_CONFIGURATION,
      response = DynamicServerResponse({ request =>
        implicit val formats = DefaultFormats
        val pageContent: String = generateConfigurationPageContent(configuration)
        StaticServerResponse(fr.simply.util.ContentType("text/html"), pageContent, 200)
      })
    )
  }

  def createServerRoutes(list: List[Route]) = {
    def loop(list: List[Route], acc: List[ServerRoute]): List[ServerRoute] = list match {
      case Nil => acc
      case x :: xs => createServerRoute(x) :: loop(xs, acc)
    }
    loop(list, List())
  }

  def createServerRoute(path: Route): ServerRoute = {

    def getRequestHeaders(request: Request): Map[String, String] = {
      val tuples: List[(String, String)] = for {
        name <- request.getNames.toArray.toList
      } yield (name.toString, request.getValue(name.toString))
      tuples.toMap
    }

    def getScenariosWithSpecifiedHeaders(scenarios: List[Scenario], headers: Map[String, String]): List[Scenario] = {
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

    def getScenariosWithMissingRequiredHeaders(scenarios: List[Scenario], headers: Map[String, String]): List[Scenario] = {
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

    if (path.scenarios == null) {
      throw new IllegalStateException("missing scenarios")
    }
    else {
      Methods.get(path.method)(
        path.path,
        Map(),
        DynamicServerResponse({ request =>
          val requestHeaders: Map[String, String] = getRequestHeaders(request)
          val scenariosWithMissingRequiredHeaders = getScenariosWithMissingRequiredHeaders(path.scenarios, requestHeaders)
          val scenariosWithMatchingHeaders = getScenariosWithSpecifiedHeaders(path.scenarios, requestHeaders)
          implicit lazy val formats = org.json4s.DefaultFormats
          // TODO: remove parse == parse
          val bodyCondition: (Scenario) => Boolean = s => parse(Serialization.write(s.request.body)) == parse(request.getContent)
          // TODO: remove parse == parse
          val emptyBodyCondition: (Scenario) => Boolean = s => parse(Serialization.write(s.request.body)) == parse("{}")
          if (scenariosWithMissingRequiredHeaders.nonEmpty) {
            val scenario = scenariosWithMissingRequiredHeaders.head
            val response: ScenarioResponse = scenario.response
            val missingHeader: (String, String) = scenario.request.headers.filter(h => !requestHeaders.contains(h._1.substring(1)) || (h._2 != null && requestHeaders.getOrElse(h._1.substring(1), null) != h._2)).head
            var serializedBody: String = Serialization.write(response.body)
            if (serializedBody.contains(EXPECTED_HEADER))
              serializedBody = serializedBody.replaceAll(EXPECTED_HEADER, missingHeader._1.substring(1))
            StaticServerResponse(fr.simply.util.ContentType(response.headers.getOrElse("Content-Type", "application/json")), serializedBody, response.code, Map("dummy_scenario" -> scenario.name))
          } else if (scenariosWithMatchingHeaders.isEmpty) {
            StaticServerResponse(APPLICATION_JSON, """{"contract_error":"no any scenarios with specified header"}""", 503)
          } else if (scenariosWithMatchingHeaders.exists(emptyBodyCondition) || scenariosWithMatchingHeaders.exists(bodyCondition)) {
            implicit lazy val formats = org.json4s.DefaultFormats
            val scenario: Scenario = if (request.getContent.isEmpty || request.getContent == "{}")
              scenariosWithMatchingHeaders.filter(emptyBodyCondition).head
            else
              scenariosWithMatchingHeaders.filter(bodyCondition).head
            StaticServerResponse(fr.simply.util.ContentType(scenario.response.headers.getOrElse("Content-Type", "application/json")), Serialization.write(scenario.response.body), scenario.response.code, scenario.response.headers)
          } else {
            StaticServerResponse(APPLICATION_JSON, """{"contract_error":"no any scenarios with specified body"}""", 503)
          }
        })
      )
    }
  }

  def parseBody(x: JsonAST.JValue): String = {
    def jsonFromFile(name: String): JValue = {
      parse(scala.io.Source.fromInputStream(getClass.getClassLoader.getResource(name).openStream()).getLines().mkString)
    }

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

}
