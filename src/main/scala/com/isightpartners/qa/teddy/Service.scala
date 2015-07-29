/**
 * iSIGHT Partners, Inc. Proprietary
 */

package com.isightpartners.qa.teddy

import com.isightpartners.qa.teddy.db.{DB, ESDB}
import com.isightpartners.qa.teddy.model.{Action, Configuration}
import com.typesafe.config.ConfigFactory
import fr.simply.StubServer
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._
import org.json4s._

import scala.collection.mutable

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/3/15
 */
class Service(db: DB = new ESDB(elastic_home = ConfigFactory.load.getString("elastic.home"))) extends HttpQuery with ServerNames {

  object Command extends Enumeration {
    val LOAD = Value("load")
    val START = Value("start")
    val STOP = Value("stop")
    val CLEAN = Value("clean")
  }

  val servers: mutable.Map[String, StubServer] = prepareServers()

  def prepareServers(): mutable.Map[String, StubServer] = {
    val configurations: List[(String, Configuration)] = db.getAllStartedConfigurations
    if (configurations.isEmpty) {
      val result: mutable.Map[String, StubServer] = mutable.Map(serverNames.head -> Creator.createWorkingServer(serverNames.head, Constants.DEFAULT_WORKING_SERVER_DESCRIPTION, Creator.loadDefaultWorkingAPI))
      result.foreach(p => p._2.start)
      result
    } else {
      implicit lazy val formats = org.json4s.DefaultFormats
      val result: mutable.Map[String, StubServer] = configurations.foldLeft(mutable.Map[String, StubServer]())((map, elem) => {
        map.put(elem._1, Creator.createWorkingServer(elem._1, elem._2.description, Extraction.decompose(elem._2.api)))
        map
      })
      result.foreach(p => p._2.start)
      result
    }
  }

  def getStatus(name: String): JValue = {
    ping(s"http://localhost:${servers.get(name).get.portInUse}${Constants.STUB_CONFIGURATION}", name, servers.get(name).get.portInUse)
  }

  def getStatus: JValue = {
    implicit lazy val formats = org.json4s.DefaultFormats
    Extraction.decompose(servers.map(p => getStatus(p._1)))
  }

  def executeCommand(name: String, parsed: JValue): JValue = {
    implicit val formats = DefaultFormats
    val body = parsed.extract[Action]
    try {
      Command.withName(body.command) match {
        case Command.START =>
          servers.get(name).get.start
          db.setStarted(name, started = true)
          getStatus(name)
        case Command.STOP =>
          servers.get(name).get.stop
          db.setStarted(name, started = false)
          getStatus(name)
        case Command.LOAD =>
          var workingServer: StubServer = servers.get(name).get
          val description = body.configuration.description
          val loadedAPI: JValue = parsed \ "configuration" \ "api"
          workingServer.stop
          workingServer = Creator.createWorkingServer(name, description, loadedAPI)
          workingServer.start
          servers.put(name, workingServer)
          db.writeConfiguration(name, body.configuration)
          getStatus(name)
        case Command.CLEAN =>
          var workingServer: StubServer = servers.get(name).get
          workingServer.stop
          workingServer = Creator.createWorkingServer(name, Constants.DEFAULT_WORKING_SERVER_DESCRIPTION, Creator.loadDefaultWorkingAPI)
          workingServer.start
          servers.put(name, workingServer)
          db.deleteConfiguration(name)
          getStatus(name)
      }
    } catch {
      case _: Throwable => "message" -> s"not supported operation '${body.command}'"
    }
  }

  def createServer(): JValue = {
    val freeNames: List[String] = serverNames.filter(!servers.keySet.contains(_))
    if (freeNames.nonEmpty) {
      val name = freeNames.head
      val workingServer = Creator.createWorkingServer(name, Constants.DEFAULT_WORKING_SERVER_DESCRIPTION, Creator.loadDefaultWorkingAPI)
      servers.put(name, workingServer)
      getStatus(name)
    } else "message" -> "reached limit of servers"
  }

  def deleteServer(name: String): Unit = {
    servers.get(name).get.stop
    servers.remove(name)
  }

}
