package com.isightpartners.qa.teddy.engine

import com.isightpartners.qa.teddy._
import com.isightpartners.qa.teddy.creator.Creator
import com.isightpartners.qa.teddy.db.{DB, ESDB}
import com.isightpartners.qa.teddy.model.{Configuration, Path}
import com.typesafe.config.ConfigFactory
import fr.simply.StubServer
import org.json4s.Extraction
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization

import scala.collection.mutable

/**
 * Created by ievgen on 29/07/15.
 */
class StubEngine(creator: Creator, db: DB = new ESDB(elastic_home = ConfigFactory.load.getString("elastic.home"))) extends Engine with HttpQuery with ServerNames {

  override val servers: mutable.Map[String, StubServer] = prepareServers()

  override def create(): JValue = {
    val freeNames: List[String] = serverNames.filter(!servers.keySet.contains(_))
    if (freeNames.nonEmpty) {
      val name = freeNames.head
      val workingServer = creator.createDefaultServer(name)
      servers.put(name, workingServer)
      status(name)
    } else "message" -> "reached limit of servers"

  }

  override def delete(name: String): Unit = {
    servers.get(name).get.stop
    servers.remove(name)
  }

  override def start(name: String): JValue = {
    servers.get(name).get.start
    db.setStarted(name, started = true)
    status(name)
  }

  override def stop(name: String): JValue = {
    servers.get(name).get.stop
    db.setStarted(name, started = false)
    status(name)
  }

  override def load(name: String, configuration: Configuration): JValue = {
    var workingServer: StubServer = servers.get(name).get
    val api: List[Path] = configuration.api
    implicit lazy val formats = org.json4s.DefaultFormats
    val loadedAPI: JValue = parse(Serialization.write(api))
    workingServer.stop
    workingServer = creator.createWorkingServer(name, configuration.description, loadedAPI)
    workingServer.start
    servers.put(name, workingServer)
    db.writeConfiguration(name, configuration)
    status(name)
  }

  override def clean(name: String): JValue = {
    var workingServer: StubServer = servers.get(name).get
    workingServer.stop
    workingServer = creator.createDefaultServer(name)
    workingServer.start
    servers.put(name, workingServer)
    db.deleteConfiguration(name)
    status(name)
  }

  def status(name: String): JValue = {
    ping(s"http://localhost:${servers.get(name).get.portInUse}${creator.STUB_CONFIGURATION}", name, servers.get(name).get.portInUse)
  }

  def statusAll: JValue = {
    implicit lazy val formats = org.json4s.DefaultFormats
    Extraction.decompose(servers.map(p => status(p._1)))
  }

  def prepareServers(): mutable.Map[String, StubServer] = {
    val configurations: List[(String, Configuration)] = db.getAllStartedConfigurations
    val result = {
      if (configurations.isEmpty) {
        loadDefaultConfiguration
      } else {
        loadConfigurations(configurations)
      }
    }
    result.foreach(p => p._2.start)
    result
  }

  def loadConfigurations(configurations: List[(String, Configuration)]): mutable.Map[String, StubServer] = {
    implicit lazy val formats = org.json4s.DefaultFormats
    configurations.foldLeft(mutable.Map[String, StubServer]())((map, elem) => {
      map.put(elem._1, creator.createWorkingServer(elem._1, elem._2.description, Extraction.decompose(elem._2.api)))
      map
    })
  }

  def loadDefaultConfiguration: mutable.Map[String, StubServer] = {
    mutable.Map(serverNames.head -> creator.createDefaultServer(serverNames.head))
  }
}
