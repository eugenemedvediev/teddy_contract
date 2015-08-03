/**
 * iSIGHT Partners, Inc. Proprietary
 */

package com.isightpartners.qa.teddy.creator

import java.util

import com.isightpartners.qa.teddy.model.Scenario
import fr.simply._
import fr.simply.util.ContentType
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.simpleframework.http.Request

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/3/15
 */
object DummyCreator extends Creator {

  override val defaultAPISettingsKey: String = "working.server.api"
  val METHOD: String = "method"
  val PATH: String = "path"
  val CONTENT_TYPE: String = "contentType"
  val CODE: String = "code"
  val APPLICATION_JSON: ContentType = fr.simply.util.ContentType("application/json")

  def generateServerRoute(x: JValue): ServerRoute = {
    implicit lazy val formats = org.json4s.DefaultFormats
    if (x \ "scenarios" == JNothing || (x \ "scenarios").extract[List[Scenario]] == null) {
      throw new IllegalStateException("missing scenarios")
    }
    else {
      val scenarios = (x \ "scenarios").extract[List[Scenario]]
      val method = (x \ METHOD).extract[String]
      val path = (x \ PATH).extract[String]
      createServerRoute(method, path, scenarios)
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

  def createServerRoute(method: String, path: String, scenarios: List[Scenario]): ServerRoute = {
    getMethod(method)(
      path,
      Map(),
      DynamicServerResponse({ request =>
        val headers: Map[String, String] = getHeaders(request)
        val filteredScenarios = getScenariosWithHeaders(scenarios, headers)
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
