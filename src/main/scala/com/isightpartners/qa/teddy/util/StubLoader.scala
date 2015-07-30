/**
 * iSIGHT Partners, Inc. Proprietary
 */

package com.isightpartners.qa.teddy.util

import java.io.{File, InputStream}
import java.util.Date

import com.isightpartners.qa.teddy.HttpQuery
import com.isightpartners.qa.teddy.model.{Server => ServerModel, Configuration}
import org.apache.http.client.methods._
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.HttpClients
import org.json4s.JValue
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/7/15
 */
object StubLoader extends App {

  case class Server(host: String, port: Int, root: String)

  val httpQuery = new HttpQuery{}

  val jsonProperties = Map(
    "Accept" -> "application/json",
    "Content-Type" -> "application/json"
  )

  def backup(host: String, port: Int, root: String): Unit = {
    def httpGetToFile(url: String): String = {
      val get = new HttpGet(url)
      setProperties(get, jsonProperties)
      val response = HttpClients.createDefault().execute(get)
      if (response.getStatusLine.getStatusCode == 200)
        writeToFile(response)
      else
        throw new IllegalStateException(s"Error during getting status to file")
    }


    def writeToFile(response: CloseableHttpResponse): String = {
      if (response.getEntity != null && response.getEntity.getContent != null) {
        val content: InputStream = response.getEntity.getContent
        val tempFile: File = java.io.File.createTempFile("status_%s".format(new Date().getTime.toString), ".json")
        inputToFile(content, tempFile)
        content.close()
        response.close()
        tempFile.getAbsolutePath
      }
      else
        throw new IllegalStateException("Error during saving file")
    }

    def inputToFile(is: java.io.InputStream, f: java.io.File) {
      val in = scala.io.Source.fromInputStream(is)
      val out = new java.io.PrintWriter(f)
      try {
        in.getLines().foreach(out.print)
      }
      finally {
        out.close()
      }
    }

    try {
      val backupFile: String = httpGetToFile(s"http://$host:$port$root/servers")
      println(s"Backup file: $backupFile")
    } catch {
      case ex: Throwable => println("Failed to backup:\n%s".format(ex.printStackTrace()))
    }
  }

  def restore(host: String, port: Int, root: String, file: String): Unit = {
    def createServer(): JValue = {
      val post = new HttpPost(s"http://$host:$port$root/servers")
      setProperties(post, jsonProperties)
      val response = HttpClients.createDefault().execute(post)
      val code: Int = response.getStatusLine.getStatusCode
      if (code == 200)
        httpQuery.getContent(response)
      else
        throw new IllegalStateException("Error during getting status")

    }

    def putCommand(name: String, payload: String): JValue = {
      val put = new HttpPut(s"http://$host:$port$root/servers/$name")
      setProperties(put, jsonProperties)
      put.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON))
      val response = HttpClients.createDefault().execute(put)
      val code: Int = response.getStatusLine.getStatusCode
      if (code == 200)
        httpQuery.getContent(response)
      else
        throw new IllegalStateException(s"Put command error for server $name")

    }

    def loadServer(server: ServerModel): Unit = {
      if (server.started) {
        implicit lazy val formats = org.json4s.DefaultFormats
        val payload: String = """{
                                | "command":"load",
                                | "configuration":{
                                |   "description":"%s",
                                |   "api":%s
                                | }
                                |}""".stripMargin.format(server.description, Serialization.write(server.api))
        putCommand(server.name, payload)
      } else {
        httpDelete(s"http://$host:$port$root/servers/${server.name}")
      }
    }

    def deleteRedundantServers(list: List[ServerModel]): Unit = {
      val startedServers: List[String] = list.filter(server => server.started).map(server => server.name)
      for (server <- getAllServerNames) {
        if (!startedServers.contains(server)) {
          httpDelete(s"http://$host:$port$root/servers/$server")
        }
      }
    }

    def getAllServerNames: List[String] = {
      val status: JValue = httpGet(s"http://$host:$port$root/servers")
      implicit lazy val formats = org.json4s.DefaultFormats
      val servers: List[ServerModel] = status.extract[Array[ServerModel]].toList
      servers.map(server => server.name)
    }

    def httpGet(url: String): JValue = {
      val get = new HttpGet(url)
      setProperties(get, jsonProperties)
      val response = HttpClients.createDefault().execute(get)
      if (response.getStatusLine.getStatusCode == 200)
        httpQuery.getContent(response)
      else
        throw new IllegalStateException(s"Error during getting url $url")
    }

    def httpDelete(url: String): Unit = {
      val delete = new HttpDelete(url)
      setProperties(delete, jsonProperties)
      val response = HttpClients.createDefault().execute(delete)
      if (response.getStatusLine.getStatusCode != 204) {
        throw new IllegalStateException("Error during deleting server")
      }
    }

    val json: JValue = parse(scala.io.Source.fromFile(file).getLines().mkString)
    implicit lazy val formats = org.json4s.DefaultFormats
    val servers: List[ServerModel] = json.extract[Array[ServerModel]].toList
    while (!createServer().extract[Map[String, Object]].getOrElse("message", "").equals("reached limit of servers")) {
    }
    servers.foreach(loadServer)
    deleteRedundantServers(servers)
    println(s"restored: GET http://$host:$port$root/servers")
  }


  def loadServer(host: String, name: String, file: String): Unit = {

    def putCommand(name: String, payload: String): JValue = {
      val put = new HttpPut(s"$host/servers/$name")
      setProperties(put, jsonProperties)
      put.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON))
      val response = HttpClients.createDefault().execute(put)
      val code: Int = response.getStatusLine.getStatusCode
      if (code == 200)
        httpQuery.getContent(response)
      else
        throw new IllegalStateException(s"Put command error for server $name")

    }

    def loadConfiguration(configuration: Configuration): Unit = {
        implicit lazy val formats = org.json4s.DefaultFormats
        val payload: String = """{
                                | "command":"load",
                                | "configuration":{
                                |   "description":"%s",
                                |   "api":%s
                                | }
                                |}""".stripMargin.format(configuration.description, Serialization.write(configuration.api))
        putCommand(name, payload)
    }

    val json: JValue = parse(scala.io.Source.fromFile(file).getLines().mkString)
    implicit lazy val formats = org.json4s.DefaultFormats
    loadConfiguration(json.extract[Configuration])
    println(s"restored: GET $host/servers/$name")
  }

  def setProperties(request: HttpRequestBase, properties: Map[String, String]) = properties.foreach {
    case (name, value) => request.setHeader(name, value)
  }

  def printHelp(message: String): Unit = {
    println(s"$message: ")
    println("\tbackup <host> <port> <root> (backup 10.102.50.24 8080 /stub)")
    println("\trestore <host> <port> <root> <file> (restore localhost 8080 /stub /var/folders/nx/tpm0cxm17ds1pxg3x75nx9340000gn/T/status_14284185215115647502452719358028.json)")
    println("\tloadServer <stub_host_root> <server_name> (loadServer http://localhost:8080/stub Angel configurations/wires.json)")
  }

  if (args.length > 0) {
    args(0) match {
      case "loadServer" => args.length match {
        case 4 => loadServer(host = args(1), name = args(2), file = args(3))
        case _ => printHelp("not correct arguments size fo command loadServer")
      }
      case "restore" => args.length match {
        case 5 => restore(host = args(1), port = args(2).toInt, root = args(3), file = args(4))
        case _ => printHelp("not correct arguments size fo command restore")
      }
      case "backup" => args.length match {
        case 4 => backup(host = args(1), port = args(2).toInt, root = args(3))
        case _ => printHelp("not correct arguments size fo command backup")
      }
      case command: String => printHelp(s"unknown command: $command")
    }
  } else {
    printHelp("not correct params")
  }

}

