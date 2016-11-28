/**
 * nl.medvediev.qa
 */

package nl.medvediev.qa.teddy.util

import nl.medvediev.qa.teddy.model.Configuration
import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
 *
 * @author Ievgen Medvediev
 * @since 4/17/15
 */
object Content extends App {

  private val file = "configurations/wires.json"
  val json = parse(scala.io.Source.fromFile(file).getLines().mkString)
  implicit lazy val formats = org.json4s.DefaultFormats
  val configuration = json.extract[Configuration]
  private val map = configuration.api.map(p => (p.path, p.method)).groupBy(p => p._1)
  private val stringToStrings = map.map(p => (p._1, p._2.map(k => k._2))).toList.sortBy(_._1)
  stringToStrings.foreach(p => println( s"""${p._1} - ${p._2.mkString(", ")}"""))
}
