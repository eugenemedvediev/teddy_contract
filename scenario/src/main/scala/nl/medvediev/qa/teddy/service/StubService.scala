package nl.medvediev.qa.teddy.service

import nl.medvediev.qa.teddy._
import nl.medvediev.qa.teddy.creator.Creator
import nl.medvediev.qa.teddy.db.DB
import nl.medvediev.qa.teddy.model.Configuration
import fr.simply.StubServer
import org.json4s.Extraction
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._

import scala.collection.mutable

/**
 * Created by ievgen on 29/07/15.
 */
class StubService(creator: Creator, db: DB) extends Service with HttpQuery with ServerNames {

  override val servers: mutable.Map[String, StubServer] = prepareServers()

  def create(configuration: Configuration): JValue = {
    val freeNames: List[String] = serverNames.filter(!servers.keySet.contains(_))
    if (freeNames.nonEmpty) {
      val name = freeNames.head
      load(name, configuration)
//      db.writeConfiguration(name, configuration)
      status(name)
    } else "message" -> "reached limit of servers"
  }

  def reset(): Unit = {
    creator.reset()
  }

  def clean(): Unit = {
    for (name <- serverNames.filter(servers.keySet.contains)) {
      delete(name)
    }
  }

  def update(name: String, configuration: Configuration): JValue = {
    val workingServer: StubServer = servers.get(name).get
    workingServer.stop
    load(name, configuration)
//    db.writeConfiguration(name, configuration)
    status(name)
  }

  def delete(name: String): Unit = {
    servers.get(name).get.stop
    servers.remove(name)
  }

  def load(name: String, configuration: Configuration): Option[StubServer] = {
    val workingServer = creator.createWorkingServer(8090, configuration.description, configuration.api)
    workingServer.start
    servers.put(name, workingServer)
  }

  def status(name: String): JValue = {
    ping(s"http://localhost:${servers.get(name).get.portInUse}${creator.DUMMY_CONFIGURATION}", name, servers.get(name).get.portInUse)
  }

  def statusAll: JValue = {
    implicit lazy val formats = org.json4s.DefaultFormats
    Extraction.decompose(servers.map(p => status(p._1)))
  }

  def prepareServers(): mutable.Map[String, StubServer] = {
    val configurations: List[(String, Configuration)] = List[(String, Configuration)]()//db.getAllStartedConfigurations
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
      map.put(elem._1, creator.createWorkingServer(8090, elem._2.description, elem._2.api))
      map
    })
  }

  def loadDefaultConfiguration: mutable.Map[String, StubServer] = {
    mutable.Map[String, StubServer]()
  }
}
