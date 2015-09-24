package qa.dummy

/**
 *
 * @author Ievgen Medvediev
 * @since 4/3/15
 */

import fr.simply._
import fr.simply.util._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import qa.common.Util
import qa.common.http.Methods
import qa.common.model.{Configuration, Route, Scenario}

object DummyCreator {

  val DUMMY_CONFIGURATION = "/_dummy_"
  val ACCEPT_HEADER = "Accept"
  val CONTENT_TYPE_HEADER = "Content-Type"
  val APPLICATION_JSON = "application/json"
  val TEXT_HTML = "text/html"
  val APPLICATION_JSON_CONTENT_TYPE = fr.simply.util.ContentType(APPLICATION_JSON)
  val TEXT_HTML_CONTENT_TYPE = fr.simply.util.Text_Html
  val TEXT_PLAIN_CONTENT_TYPE = fr.simply.util.Text_Plain
  val EXPECTED_HEADER = "@expectedHeader"

  def createServer(port: Int, configuration: Configuration) = {
    val routes: List[ServerRoute] = createConfigurationRoute(configuration) :: createServerRoutes(configuration.api)
    new StubServer(port, routes.toArray: _*).defaultResponse(APPLICATION_JSON_CONTENT_TYPE, ContractError.NOT_SUPPORTED_PATH, 503)
  }

  def createConfigurationRoute(configuration: Configuration): ServerRoute = {

    def generateConfigurationHtmlContent(configuration: Configuration): String = {
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

    def generateConfigurationJsonContent(configuration: Configuration): String = {
      implicit val formats = DefaultFormats
      pretty(parse(Serialization.write(configuration)))
    }

    def textPlainResponse: StaticServerResponse = {
      val pageContent: String = generateConfigurationJsonContent(configuration)
      StaticServerResponse(TEXT_PLAIN_CONTENT_TYPE, pageContent, 200)
    }

    GET(
      path = DUMMY_CONFIGURATION,
      response = DynamicServerResponse({ request =>
        val headers = Util.getRequestHeaders(request)
        headers.get(ACCEPT_HEADER) match {
          case Some(contentType) => {
            contentType match {
              case value if value.contains(APPLICATION_JSON) =>
                StaticServerResponse(APPLICATION_JSON_CONTENT_TYPE, generateConfigurationJsonContent(configuration), 200)
              case value if value.contains(TEXT_HTML) =>
                StaticServerResponse(TEXT_HTML_CONTENT_TYPE, generateConfigurationHtmlContent(configuration), 200)
              case _ => textPlainResponse
            }
          }
          case None => textPlainResponse
        }
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

  def createServerRoute(route: Route): ServerRoute = {

    def expectedResponse(request: org.simpleframework.http.Request, scenario: Scenario): StaticServerResponse = {
      implicit lazy val formats = org.json4s.DefaultFormats
      val contentType: String = scenario.response.headers.getOrElse(CONTENT_TYPE_HEADER, APPLICATION_JSON)
      var body = scenario.response.body.toString
      if (contentType.equals(APPLICATION_JSON)) {
        try {
          body = parseBody(parse(Serialization.write(scenario.response.body)))
          StaticServerResponse(ContentType(contentType), body, scenario.response.code, scenario.response.headers)
        } catch {
          case ex: Throwable =>
            StaticServerResponse(APPLICATION_JSON_CONTENT_TYPE, ContractError.CONTRACT_ERROR.format(ex.getMessage), 503, Map("dummy_scenario" -> scenario.name))
        }
      } else {
        StaticServerResponse(ContentType(contentType), body, scenario.response.code, scenario.response.headers)
      }
    }

    def exceptionalResponse(scenariosWithMissingRequiredHeaders: List[Scenario], scenariosWithSpecifiedHeaders: List[Scenario], requestHeaders: Map[String, String]): StaticServerResponse = {
      if (scenariosWithMissingRequiredHeaders.nonEmpty)
        withoutRequiredHeaderResponse(scenariosWithMissingRequiredHeaders, requestHeaders)
      else if (scenariosWithSpecifiedHeaders.isEmpty)
        StaticServerResponse(APPLICATION_JSON_CONTENT_TYPE, ContractError.NO_SCENARIO_WITH_HEADER, 503)
      else
        StaticServerResponse(APPLICATION_JSON_CONTENT_TYPE, ContractError.NO_SCENARIO_WITH_BODY, 503)
    }

    def withoutRequiredHeaderResponse(scenariosWithoutRequiredHeaders: List[Scenario], requestHeaders: Map[String, String]): StaticServerResponse = {
      implicit lazy val formats = org.json4s.DefaultFormats
      val scenario = scenariosWithoutRequiredHeaders.head
      val response = scenario.response
      val requiredHeader: (String, String) = scenario.request.headers.filter(h => !requestHeaders.contains(h._1.substring(1)) || (h._2 != null && requestHeaders.getOrElse(h._1.substring(1), null) != h._2)).head
      //TODO write test and simplify
      var serializedBody: String = Serialization.write(response.body)
      if (serializedBody.contains(EXPECTED_HEADER))
        serializedBody = serializedBody.replaceAll(EXPECTED_HEADER, requiredHeader._1.substring(1))
      StaticServerResponse(fr.simply.util.ContentType(response.headers.getOrElse(CONTENT_TYPE_HEADER, APPLICATION_JSON)), serializedBody, response.code, Map("dummy_scenario" -> scenario.name))
    }

    if (route.scenarios == null) {
      throw new IllegalStateException("missing scenarios")
    }
    else {
      Methods.get(route.method)(
        route.path,
        Map(),
        DynamicServerResponse({ request =>
          val requestHeaders: Map[String, String] = Util.getRequestHeaders(request)
          val scenariosWithMissingRequiredHeaders = getScenariosWithMissingRequiredHeaders(route.scenarios, requestHeaders)
          val scenariosWithSpecifiedHeaders = getScenariosWithSpecifiedHeaders(route.scenarios, requestHeaders)

          val content = request.getContent
          val hasRequiredHeaders: Boolean = scenariosWithMissingRequiredHeaders.isEmpty
          val hasSpecifiedHeaders: Boolean = scenariosWithSpecifiedHeaders.nonEmpty
          val hasBodyMatch: Boolean = scenariosWithSpecifiedHeaders.exists(bodyCondition(content))

          if (hasRequiredHeaders && hasSpecifiedHeaders && hasBodyMatch) {
            val scenario: Scenario = scenariosWithSpecifiedHeaders.filter(bodyCondition(content)).head
            expectedResponse(request, scenario)
          } else {
            exceptionalResponse(scenariosWithMissingRequiredHeaders, scenariosWithSpecifiedHeaders, requestHeaders)
          }
        })
      )
    }
  }

  def getScenariosWithSpecifiedHeaders(scenarios: List[Scenario], headers: Map[String, String]): List[Scenario] = {
    if (scenarios == null || scenarios.isEmpty) {
      List[Scenario]()
    } else if (headers == null || headers.isEmpty) {
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
    if (scenarios == null || scenarios.isEmpty)
      List[Scenario]()
    else {
      val withRequiredHeaders: List[Scenario] = scenarios.filter(scenario =>
        scenario.request.headers.exists(h => h._1.startsWith("!"))
      )
      if (headers == null || headers.isEmpty)
        withRequiredHeaders
      else
        withRequiredHeaders.filter(scenario =>
          scenario.request.headers.exists(h => {
            val key: String = h._1.substring(1)
            !headers.contains(key) || (h._2 != null && headers.getOrElse(key, null) != h._2)
          })
        )
    }

  }

  def parseBody(body: JsonAST.JValue): String = {
    implicit lazy val formats = org.json4s.DefaultFormats
    if (body == null) return null
    body match {
      case _: JArray => compact(body)
      case _: JObject => compact(body)
      case value: JString => {
        value match {
          case string if string.extract[String].startsWith("@") => {
            try {
              compact(Util.jsonFromFile(string.extract[String].substring(1)))
            } catch {
              case _: Throwable => throw new scala.IllegalArgumentException(s"Can't load body from file: ${string.extract[String].substring(1)}")
            }
          }
          case _ => compact(value)
        }
      }
      case _ => throw new scala.IllegalArgumentException(s"Not supported body: $body")
    }
  }

  def bodyCondition(content: String): (Scenario) => Boolean = { scenario =>
    implicit lazy val formats = org.json4s.DefaultFormats
    if (scenario.request.body == null) true
    else if (content == null || content.isEmpty) false
    else parse(content) == parse(Serialization.write(scenario.request.body))
  }

}
