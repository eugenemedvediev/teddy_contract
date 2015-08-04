package com.isightpartners.qa.teddy.creator

import com.isightpartners.qa.teddy.creator.DummyCreator._
import com.isightpartners.qa.teddy.model.{Scenario, Path}
import fr.simply.{StaticServerResponse, DynamicServerResponse, ServerRoute, StubServer}
import org.json4s.JValue
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization


/**
 * Created by ievgen on 30/07/15.
 */
object ScenarioCreator extends Creator {

  var index = 0
  override def createServerRoutes(list: List[Path]): List[ServerRoute] = {
    val myMap = collection.mutable.Map[(String, String), Map[Int, Scenario]]()
    val map1: List[((String, String), Scenario)] = list.map(path => ((path.path, path.method), path.scenarios.head))
    for (i <- 0 to map1.size-1){
      val key: (String, String) = map1(i)._1
      val value: Scenario = map1(i)._2

      val existingPath: Map[Int, Scenario] = myMap.getOrElse(key, null)
      if (existingPath == null) {
        myMap.put(key, Map(i -> value))
      } else {
        myMap.put(key, existingPath + (i -> value))
      }
    }

    myMap.foldLeft(List[ServerRoute]())( (list, elem) => createServerRoute(elem)::list)

  }

  def createServerRoute(elem: ((String, String), Map[Int, Scenario])): ServerRoute = {
    getMethod(elem._1._2)(
      elem._1._1,
      Map(),
      DynamicServerResponse({ request =>
        val headers: Map[String, String] = getHeaders(request)
        val filteredScenarios = getScenariosWithHeaders(elem._2.values.toList, headers)
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
          if (elem._2.getOrElse(index, null) == scenario){
            index += 1
            StaticServerResponse(fr.simply.util.ContentType(scenario.response.headers.getOrElse("Content-Type", "application/json")), Serialization.write(scenario.response.body), scenario.response.code, scenario.response.headers)
          } else {
            StaticServerResponse(APPLICATION_JSON, """{"scenario_error":"not acceptable step"}""", 503)
          }
        } else {
          StaticServerResponse(APPLICATION_JSON, """{"contract_error":"no any scenarios with specified body"}""", 503)
        }
      })
    )
  }

}
