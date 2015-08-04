/**
 * iSIGHT Partners, Inc. Proprietary
 */

package com.isightpartners.qa.teddy.creator

import com.isightpartners.qa.teddy.model.{Path, Scenario}
import fr.simply._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/3/15
 */
object DummyCreator extends Creator {

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
          val filteredScenarios = getScenariosWithHeaders(path.scenarios, headers)
          implicit lazy val formats = org.json4s.DefaultFormats
          val bodyCondition: (Scenario) => Boolean = s => parse(Serialization.write(s.request.body)) == parse(request.getContent)
          val emptyBodyCondition: (Scenario) => Boolean = s => parse(Serialization.write(s.request.body)) == parse("{}")
          if (filteredScenarios.isEmpty) {
            StaticServerResponse(APPLICATION_JSON, """{"contract_error":"no any scenarios with specified header"}""", 503)
          } else if (filteredScenarios.exists(emptyBodyCondition) || filteredScenarios.exists(bodyCondition)) {
            implicit lazy val formats = org.json4s.DefaultFormats
            println("test" + request.getContent)
            val scenario: Scenario = if (request.getContent.isEmpty || request.getContent == "{}")
              filteredScenarios.filter(emptyBodyCondition).head
            else
              filteredScenarios.filter(bodyCondition).head
            StaticServerResponse(fr.simply.util.ContentType(scenario.response.headers.getOrElse("Content-Type", "application/json")), Serialization.write(scenario.response.body), scenario.response.code, scenario.response.headers)
          } else {
            StaticServerResponse(APPLICATION_JSON, """{"contract_error":"no any scenarios with specified body"}""", 503)
          }
        })
      )
    }
  }

}
