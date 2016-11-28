package nl.medvediev.qa.teddy.service

import nl.medvediev.qa.teddy.model.Configuration
import fr.simply.StubServer
import org.json4s.JsonAST.JValue

import scala.collection.mutable

/**
 * Created by ievgen on 29/07/15.
 */
trait Service {
  val servers: mutable.Map[String, StubServer]

  def create(configuration: Configuration): JValue
  def update(name: String, configuration: Configuration): JValue
  def delete(name: String): Unit
  def clean(): Unit
  def reset(): Unit
  def status(name: String): JValue
  def statusAll(): JValue
}
