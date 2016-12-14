package qa.common

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import qa.common.model.{Configuration, ScenarioRequest}

object Util {

  def getStringifiedRoutes(configuration: Configuration): List[String] = {
    val groupedByPath = configuration.api.map(r => (r.path, r.method)).groupBy(_._1)
    val sortedByPath = groupedByPath.map(p => (p._1, p._2.map(k => k._2))).toList.sortBy(_._1)
    val strings = sortedByPath.map(p => s"""${p._1} - ${p._2.mkString(", ")}""")
    strings
  }

  def getCurlRoutes(port: Int, configuration: Configuration): List[(String, String)] = {
    implicit val formats = DefaultFormats
    val scenarios  = for {
      route <- configuration.api
      scenario <- route.scenarios
    } yield (route.method, route.path, scenario.request, scenario.name)
    scenarios.map {
      case (method: String, path: String, request: ScenarioRequest, name: String) => {
        val headers = request.headers.map(header => s"""-H "${header._1}: ${header._2}"""")
        val queries = request.query.map(query => s"""${query._1}=${query._2}""")
        val body = request.body
        (
          name,
          s"curl -X $method" +
            s""" "http://localhost:$port$path${if (queries.nonEmpty) "?" + queries.mkString("&") else ""}"""" +
            {if (headers.nonEmpty) " " + headers.mkString(" ") else ""} +
            {if (body != null) " --data " + "'" + pretty(parse(Serialization.write(body))) + "'" else ""
          }
        )
      }
    }
  }

  def jsonFromFile(name: String): JValue = {
    parse(scala.io.Source.fromFile(name).getLines().mkString)
  }

  def extractConfiguration(file: String): Configuration = {
    val json: JValue = jsonFromFile(file)
    implicit val formats = DefaultFormats
    val configuration = json.extract[Configuration]
    configuration
  }


}
