package qa.common

import qa.common.model.Configuration

/**
 * Created by ievgen on 22/09/15.
 */
object Util {

  def getStringifiedRoutes(configuration: Configuration): List[String] = {
    val groupedByPath = configuration.api.map(r => (r.path, r.method)).groupBy(_._1)
    val sortedByPath = groupedByPath.map(p => (p._1, p._2.map(k => k._2))).toList.sortBy(_._1)
    val strings = sortedByPath.map(p => s"""${p._1} - ${p._2.mkString(", ")}""")
    strings
  }

}
