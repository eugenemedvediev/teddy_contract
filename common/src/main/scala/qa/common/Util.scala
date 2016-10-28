package qa.common

import org.json4s._
import org.json4s.jackson.JsonMethods._
import qa.common.model.Configuration

object Util {

  def getStringifiedRoutes(configuration: Configuration): List[String] = {
    val groupedByPath = configuration.api.map(r => (r.path, r.method)).groupBy(_._1)
    val sortedByPath = groupedByPath.map(p => (p._1, p._2.map(k => k._2))).toList.sortBy(_._1)
    val strings = sortedByPath.map(p => s"""${p._1} - ${p._2.mkString(", ")}""")
    strings
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
