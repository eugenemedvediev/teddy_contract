/**
 * iSIGHT Partners, Inc. Proprietary
 */

package com.isightpartners.qa.teddy.creator

import com.isightpartners.qa.teddy.model.Scenario
import fr.simply._
import fr.simply.util.ContentType
import org.json4s._
import org.json4s.jackson.Serialization

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


}
