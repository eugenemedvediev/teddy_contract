package com.isightpartners.qa.teddy.engine

import com.isightpartners.qa.teddy.db.DB
import com.isightpartners.qa.teddy.model.Configuration
import fr.simply.StubServer
import org.json4s.JsonAST.JValue

import scala.collection.mutable

/**
 * Created by ievgen on 29/07/15.
 */
trait Engine {
  val servers: mutable.Map[String, StubServer]

  def create(): JValue
  def delete(name: String): Unit
  def start(name: String): JValue
  def stop(name: String): JValue
  def load(name: String, configuration: Configuration): JValue
  def clean(name: String): JValue
  def status(name: String): JValue
  def statusAll(): JValue
}
