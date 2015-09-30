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
import org.simpleframework.http.Request
import qa.common.Util
import qa.common.exception.{ConfigurationException, ContractException}
import qa.common.http.Methods
import qa.common.model.{Configuration, Route, Scenario}

import scala.collection.JavaConversions._

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

  @throws(classOf[ConfigurationException])
  def createServer(port: Int, configuration: Configuration) = {
    configuration.api.foreach(validateRoute)
    val routes: List[ServerRoute] = createConfigurationRoute(configuration) :: createServerRoutes(configuration.api)
    new StubServer(port, routes.toArray: _*).defaultResponse(APPLICATION_JSON_CONTENT_TYPE, ContractError.NOT_SUPPORTED_PATH_ERROR, 503)
  }

  @throws(classOf[ConfigurationException])
  def validateRoute(route: Route): Unit = {
    if (route.path.isEmpty || !route.path.startsWith("/")) {
      throw new ConfigurationException( s"""Not valid path: "${route.path}"""")
    }
    if (route.method.isEmpty || !Methods.methods.keySet.contains(route.method)) {
      throw new ConfigurationException( s"""Not valid method "${route.method}" in path: "${route.path}"""")
    }
    if (route.scenarios.isEmpty) {
      throw new ConfigurationException(s"Empty scenarios for route: ${route.method} ${route.path}")
    }
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

  @throws(classOf[ConfigurationException])
  def createServerRoutes(list: List[Route]) = {
    def loop(list: List[Route], acc: List[ServerRoute]): List[ServerRoute] = list match {
      case Nil => acc
      case x :: xs => createServerRoute(x) :: loop(xs, acc)
    }
    loop(list, List())
  }

  def createServerRoute(route: Route): ServerRoute = {

    def expectedResponse(request: Request, scenario: Scenario): StaticServerResponse = {
      implicit lazy val formats = org.json4s.DefaultFormats
      val contentType: String = scenario.response.headers.getOrElse(CONTENT_TYPE_HEADER, APPLICATION_JSON)
      val body =
        if (contentType.equals(APPLICATION_JSON))
          parseBody(parse(Serialization.write(scenario.response.body)))
        else
          scenario.response.body.toString
      StaticServerResponse(ContentType(contentType), body, scenario.response.code, scenario.response.headers)
    }

    def requiredResponse(scenario: Scenario, headers: Map[String, String], query: Map[String, String]): StaticServerResponse = {
      implicit lazy val formats = org.json4s.DefaultFormats
      val response = scenario.response
      var serializedBody: String = Serialization.write(response.body)
      if (scenario.request.query.exists(q => q._1.startsWith("!"))) {

      } else {
        val requiredHeader: (String, String) =
          scenario.request.headers.filter(h =>
            !headers.contains(h._1.substring(1)) || (h._2 != null && headers.getOrElse(h._1.substring(1), null) != h._2)
          ).head
        if (serializedBody.contains(EXPECTED_HEADER))
          serializedBody = serializedBody.replaceAll(EXPECTED_HEADER, requiredHeader._1.substring(1))
      }
      StaticServerResponse(ContentType(response.headers.getOrElse(CONTENT_TYPE_HEADER, APPLICATION_JSON)), serializedBody, response.code, Map("dummy_scenario" -> scenario.name))
    }

    Methods.get(route.method)(
      route.path,
      Map(),
      DynamicServerResponse({ request =>
        try {
          val query: Map[String, String] = request.getAddress.getQuery.toMap
          val headers: Map[String, String] = Util.getRequestHeaders(request)
          val content = request.getContent
          val requiredScenario = findRequiredScenario(route.scenarios, headers, query)
          if (requiredScenario != null)
            requiredResponse(requiredScenario, headers, query)
          else {
            val scenario = findScenario(route.scenarios, headers, query, content)
            expectedResponse(request, scenario)
          }
        } catch {
          case ex: ContractException =>
            StaticServerResponse(APPLICATION_JSON_CONTENT_TYPE, ContractError.CONTRACT_ERROR.format(ex.getMessage), 503)
          case t: Throwable =>
            StaticServerResponse(APPLICATION_JSON_CONTENT_TYPE, ContractError.DUMMY_ERROR.format(t.getMessage), 503)
        }
      })
    )

  }

  def findRequiredScenario(scenarios: List[Scenario], headers: Map[String, String] = Map(), query: Map[String, String] = Map()): Scenario = {
    val withRequiredQuery = scenarios.filter(scenario =>
      scenario.request.query.nonEmpty && scenario.request.query.exists(q => q._1.startsWith("!"))
    )
    val requiredQueries = if (query == null || query.isEmpty)
      withRequiredQuery
    else
      withRequiredQuery.filter(scenario =>
        scenario.request.query.exists(q => {
          val key: String = q._1.substring(1)
          !query.contains(key) || (q._2 != null && query.getOrElse(key, null) != q._2)
        })
      )

    val withRequiredHeaders: List[Scenario] = scenarios.filter(scenario =>
      scenario.request.headers.nonEmpty && scenario.request.headers.exists(h => h._1.startsWith("!"))
    )
    val requiredHeaders = if (headers == null || headers.isEmpty)
      withRequiredHeaders
    else
      withRequiredHeaders.filter(scenario =>
        scenario.request.headers.exists(h => {
          val key: String = h._1.substring(1)
          !headers.contains(key) || (h._2 != null && headers.getOrElse(key, null) != h._2)
        })
      )

    val required = requiredQueries ::: requiredHeaders
    if (required.isEmpty) null else required.head

  }

  @throws(classOf[ContractException])
  def findScenario(scenarios: List[Scenario], headers: Map[String, String], query: Map[String, String], content: String) = {
    val withQuery = filterScenariosByQuery(scenarios, query)
    val withHeaders = filterScenariosByHeaders(withQuery, headers)
    val withBody = filterScenariosByBody(withHeaders, content)
    withBody.head
  }

  /**
   * @param scenarios is a list of scenarios which should be filtered
   * @param smap function which return map in scenario which should be compared
   * @param amap map with which scenario should be compared
   * @return list of scenarios which has all elements of amap in smap
   */
  private def filterScenarioByMap(scenarios: List[Scenario], smap: Scenario => Map[String, String], amap: Map[String, String]): List[Scenario] = {
    if (amap == null || amap.isEmpty) {
      scenarios.filter(scenario =>
        smap(scenario).isEmpty
      )
    } else {
      scenarios.filter(scenario =>
        smap(scenario).forall(sq =>
          amap.contains(sq._1) && amap.getOrElse(sq._1, null) == sq._2
        )
      )
    }
  }

  @throws(classOf[ContractException])
  def filterScenariosByHeaders(scenarios: List[Scenario], headers: Map[String, String]): List[Scenario] = {
    require(scenarios != null, "scenarios are absent")
    require(scenarios.nonEmpty, "scenarios are empty")

    val list = filterScenarioByMap(scenarios, (s: Scenario) => s.request.headers, headers)

    if (list.isEmpty) {
      throw new ContractException("no any scenarios with specified header")
    }
    list
  }

  @throws(classOf[ContractException])
  def filterScenariosByQuery(scenarios: List[Scenario], query: Map[String, String]): List[Scenario] = {
    require(scenarios != null, "scenarios are absent")
    require(scenarios.nonEmpty, "scenarios are empty")

    val list = filterScenarioByMap(scenarios, (s: Scenario) => s.request.query, query)
    if (list.isEmpty) {
      throw new ContractException("no any scenarios with specified query")
    }
    list
  }

  def filterScenariosByBody(scenarios: List[Scenario], content: String): List[Scenario] = {
    require(scenarios != null, "scenarios are absent")
    require(scenarios.nonEmpty, "scenarios are empty")

    val list = scenarios.filter(bodyCondition(content))
    if (list.isEmpty) {
      throw new ContractException("no any scenarios with specified body")
    }
    list
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

  // TODO: add support for string value, xml, html, text/plain
  def bodyCondition(content: String): (Scenario) => Boolean = { scenario =>
    implicit lazy val formats = org.json4s.DefaultFormats
    if (scenario.request.body == null) true
    else if (content == null) false
    else if (content.isEmpty) {
      content == scenario.request.body
    } else{
      parse(content) == parse(Serialization.write(scenario.request.body))
    }
  }

}
