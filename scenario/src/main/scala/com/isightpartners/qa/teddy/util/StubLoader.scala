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

  val httpQuery = new HttpQuery {}

  val jsonProperties = Map(
    "Accept" -> "application/json",
    "Content-Type" -> "application/json"
  )

  def backup(host: String, stubType: String): Unit = {
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
      val backupFile: String = httpGetToFile(s"$host/$stubType")
      println(s"Backup file: $backupFile")
    } catch {
      case ex: Throwable => println("Failed to backup:\n%s".format(ex.printStackTrace()))
    }
  }

  def restore(host: String, stubType: String, file: String): Unit = {
    def createServer(): JValue = {
      val url: String = s"$host/$stubType"
      println(url)
      val post = new HttpPost(s"$host/$stubType")
      post.setEntity(new StringEntity(
        """
          |{
          |  "description":"fake",
          |  "api":[]
          |}
        """.stripMargin, ContentType.APPLICATION_JSON))
      setProperties(post, jsonProperties)
      val response = HttpClients.createDefault().execute(post)
      val code: Int = response.getStatusLine.getStatusCode
      if (code == 200) {
        httpQuery.getContent(response)
      }
      else {
        println(s"Status code: $code")
        println(pretty(httpQuery.getContent(response)))
        throw new IllegalStateException("Error during getting status")
      }
    }

    def putCommand(name: String, payload: String): JValue = {
      val put = new HttpPut(s"$host/$stubType/$name")
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
        val payload: String =
          """{
            |   "description":"%s",
            |   "api":%s
            |}""".stripMargin.format(server.description, Serialization.write(server.api))
        putCommand(server.name, payload)
      } else {
        httpDelete(s"$host/$stubType/${server.name}")
      }
    }

    def deleteRedundantServers(list: List[ServerModel]): Unit = {
      val startedServers: List[String] = list.filter(server => server.started).map(server => server.name)
      for (server <- getAllServerNames) {
        if (!startedServers.contains(server)) {
          httpDelete(s"$host/$stubType/$server")
        }
      }
    }

    def getAllServerNames: List[String] = {
      val status: JValue = httpGet(s"$host/$stubType")
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
    var i = 1
    while (!createServer().extract[Map[String, Object]].getOrElse("message", "").equals("reached limit of servers")) {
      println(i)
      i += 1
    }
    servers.foreach(loadServer)
    deleteRedundantServers(servers)
    println(s"restored: GET $host/$stubType")
  }


  def createServer(host: String, stubType: String, file: String): Unit = {

    def postCommand(payload: String): JValue = {
      val url = s"$host/$stubType"
      val post = new HttpPost(url)
      setProperties(post, jsonProperties)
      post.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON))
      val response = HttpClients.createDefault().execute(post)
      val code: Int = response.getStatusLine.getStatusCode
      println(code)
      if (code == 200) {
        httpQuery.getContent(response)
      }
      else {
        println(s"Status code: $code")
        println(pretty(httpQuery.getContent(response)))
        throw new IllegalStateException(s"Post command error")
      }
    }

    val json: JValue = parse(scala.io.Source.fromFile(file).getLines().mkString)
    implicit lazy val formats = org.json4s.DefaultFormats
    val response: JValue = postCommand(compact(json))
    println(s"Created: \n${pretty(response)}")
  }


  def updateServer(host: String, stubType: String, name: String, file: String): Unit = {

    def putCommand(payload: String): JValue = {
      val put = new HttpPut(s"$host/$stubType/$name")
      setProperties(put, jsonProperties)
      put.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON))
      val response = HttpClients.createDefault().execute(put)
      val code: Int = response.getStatusLine.getStatusCode
      if (code == 200) {
        httpQuery.getContent(response)
      }
      else {
        println(s"Status code: $code")
        println(pretty(httpQuery.getContent(response)))
        throw new IllegalStateException(s"Put command error for server $name")
      }
    }

    val json: JValue = parse(scala.io.Source.fromFile(file).getLines().mkString)
    implicit lazy val formats = org.json4s.DefaultFormats
    val response: JValue = putCommand(compact(json))
    println(s"Updated: \n${pretty(response)}")
  }

  def setProperties(request: HttpRequestBase, properties: Map[String, String]) = properties.foreach {
    case (name, value) => request.setHeader(name, value)
  }

  def printHelp(message: String): Unit = {
    println(s"$message: ")
    println("\tbackup <stub_host> <stub_type> (backup http://10.102.50.24:8080 {scenarios | dummies})")
    println("\trestore <stub_host> <stub_type> <file_path> (restore http://localhost:8080 {scenarios | dummies} /var/folders/nx/tpm0cxm17ds1pxg3x75nx9340000gn/T/status_14284185215115647502452719358028.json)")
    println("\tupdateServer <stub_host> <stub_type> <server_name> <file_path> (updateServer http://localhost:8080 {scenarios | dummies} Angel configurations/wires.json)")
    println("\tcreateServer <stub_host> <stub_type> <file_path>(createServer http://localhost:8080 {scenarios | dummies} configurations/wires.json)")
  }

  if (args.length > 0) {
    args(0) match {
      case "updateServer" => args.length match {
        case 5 => updateServer(host = args(1), stubType = args(2), name = args(3), file = args(4))
        case _ => printHelp("not correct arguments size fo command updateServer")
      }
      case "createServer" => args.length match {
        case 4 => createServer(host = args(1), stubType = args(2), file = args(3))
        case _ => printHelp("not correct arguments size fo command createServer")
      }
      case "restore" => args.length match {
        case 4 => restore(host = args(1), stubType = args(2), file = args(3))
        case _ => printHelp("not correct arguments size fo command restore")
      }
      case "backup" => args.length match {
        case 3 => backup(host = args(1), stubType = args(2))
        case _ => printHelp("not correct arguments size fo command backup")
      }
      case command: String => printHelp(s"unknown command: $command")
    }
  } else {
    printHelp("not correct params")
  }

}

