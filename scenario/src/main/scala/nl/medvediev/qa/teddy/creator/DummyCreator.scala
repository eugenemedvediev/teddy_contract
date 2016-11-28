/**
 * nl.medvediev.qa
 */

package nl.medvediev.qa.teddy.creator

import nl.medvediev.qa.teddy.model.{ScenarioResponse, Path, Scenario}
import fr.simply._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization

/**
 *
 * @author Ievgen Medvediev
 * @since 4/3/15
 */
object DummyCreator extends Creator {

  val EXPECTED_HEADER = "@expectedHeader"
  def createServerRoutes(list: List[Path]) = {
    def loop(list: List[Path], acc: List[ServerRoute]): List[ServerRoute] = list match {
      case Nil => acc
      case x :: xs => generateServerRoute(x) :: loop(xs, acc)
    }
    loop(list, List())
  }

  def generateServerRoute(path: Path): ServerRoute = {
    if (path.scenarios == null) {
      throw new IllegalStateException("missing scenarios")
    }
    else {
      getMethod(path.method)(
        path.path,
        Map(),
        DynamicServerResponse({ request =>
          val headers: Map[String, String] = getHeaders(request)
          val scenariosWithRequiredHeaders = getScenariosWithRequiredHeaders(path.scenarios, headers)
          val scenariosWithHeaders = getScenariosWithHeaders(path.scenarios, headers)
          implicit lazy val formats = org.json4s.DefaultFormats
          val bodyCondition: (Scenario) => Boolean = s => parse(Serialization.write(s.request.body)) == parse(request.getContent)
          val emptyBodyCondition: (Scenario) => Boolean = s => parse(Serialization.write(s.request.body)) == parse("{}")
          if (!scenariosWithRequiredHeaders.isEmpty){
            val scenario = scenariosWithRequiredHeaders.head
            val response: ScenarioResponse = scenario.response
            val missingHeader: (String, String) = scenario.request.headers.filter(h => !headers.contains(h._1.substring(1)) || (h._2 != null && headers.getOrElse(h._1.substring(1), null) != h._2)).head
            var serializedBody: String = Serialization.write(response.body)
            if (serializedBody.contains(EXPECTED_HEADER))
              serializedBody = serializedBody.replaceAll(EXPECTED_HEADER, missingHeader._1.substring(1))
            StaticServerResponse(fr.simply.util.ContentType(response.headers.getOrElse("Content-Type", "application/json")), serializedBody, response.code, Map("dummy_scenario" -> scenario.name))
          }
          else if (scenariosWithHeaders.isEmpty) {
            StaticServerResponse(APPLICATION_JSON, """{"contract_error":"no any scenarios with specified header"}""", 503)
          } else if (scenariosWithHeaders.exists(emptyBodyCondition) || scenariosWithHeaders.exists(bodyCondition)) {
            implicit lazy val formats = org.json4s.DefaultFormats
//            println("test" + request.getContent)
            val scenario: Scenario = if (request.getContent.isEmpty || request.getContent == "{}")
              scenariosWithHeaders.filter(emptyBodyCondition).head
            else
              scenariosWithHeaders.filter(bodyCondition).head
            StaticServerResponse(fr.simply.util.ContentType(scenario.response.headers.getOrElse("Content-Type", "application/json")), Serialization.write(scenario.response.body), scenario.response.code, scenario.response.headers)
          } else {
            StaticServerResponse(APPLICATION_JSON, """{"contract_error":"no any scenarios with specified body"}""", 503)
          }
        })
      )
    }
  }

}
