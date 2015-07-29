/**
 * iSIGHT Partners, Inc. Proprietary
 */

import java.nio.file.{Path => FilePath}

import com.isightpartners.qa.teddy.db.DB
import com.isightpartners.qa.teddy.model.{Configuration, Path, Scenario, Server}
import com.isightpartners.qa.teddy.{Constants, HttpQuery, Service}
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/10/15
 */
class WorkingServerIntegrationTest extends FunSuite with Payload with BeforeAndAfterAll {

  case class WorkingServerConfigurationResponse(name: String, description: String, api: List[Path])

  val httpQuery = new HttpQuery
  val service: Service = new Service(new DB {
    def writeConfiguration(name: String, configuration: Configuration) = {}

    def getAllStartedConfigurations: List[(String, Configuration)] =  List[(String, Configuration)]()

    def deleteConfiguration(name: String) = {}

    def setStarted(name: String, started: Boolean) = {}

    def readConfiguration(name: String): Configuration = new Configuration()
  })

  test("default configuration ok scenario") {
    // given
    val createJson: JValue = service.createServer()
    implicit lazy val formats = org.json4s.DefaultFormats
    val createServer: Server = createJson.extract[Server]
    val startJson: JValue = service.executeCommand(createServer.name,
      "command" -> service.Command.START.toString
    )
    val server: Server = startJson.extract[Server]
    val url: String = s"http://localhost:${server.port}"

    // when
    val (code: Int, json: JValue) = httpQuery.get(s"$url/fake")

    // then
    assert(code === 200)
    val expected: JValue = "message" -> "fake server successful response"
    assert(json === expected)
  }

  test("default configuration validation scenario") {
    // given
    val createJson: JValue = service.createServer()
    implicit lazy val formats = org.json4s.DefaultFormats
    val createServer: Server = createJson.extract[Server]
    val startJson: JValue = service.executeCommand(createServer.name,
      "command" -> service.Command.START.toString
    )
    val server: Server = startJson.extract[Server]
    val url: String = s"http://localhost:${server.port}"

    // when
    val (code: Int, json: JValue) = httpQuery.get(s"$url/fake_validation")

    // then
    assert(code === 400)
    val expected: JValue = "error" -> "example of validation error on fake server"
    assert(json === expected)
  }

  test("default configuration server_error scenario") {
    // given
    val createJson: JValue = service.createServer()
    implicit lazy val formats = org.json4s.DefaultFormats
    val createServer: Server = createJson.extract[Server]
    val startJson: JValue = service.executeCommand(createServer.name,
      "command" -> service.Command.START.toString
    )
    val server: Server = startJson.extract[Server]
    val url: String = s"http://localhost:${server.port}"

    // when
    val (code: Int, json: JValue) = httpQuery.get(s"$url/fake_server_error")

    // then
    assert(code === 503)
    val expected: JValue = "error" -> "example of server error on fake server"
    assert(json === expected)
  }

  test("default configuration not existing scenario") {
    // given
    val createJson: JValue = service.createServer()
    implicit lazy val formats = org.json4s.DefaultFormats
    val createServer: Server = createJson.extract[Server]
    val startJson: JValue = service.executeCommand(createServer.name,
      "command" -> service.Command.START.toString
    )
    val server: Server = startJson.extract[Server]
    val url: String = s"http://localhost:${server.port}"

    // when
    val (code: Int, json: JValue) = httpQuery.get(s"$url/fake_929dskfs0ewkl")

    // then
    assert(code === 404)
    val expected: JValue = "contract_error" -> "not supported path or method by contract; check configuration GET /stub/configuration"
    assert(json === expected)
  }

  test("configuration page") {
    // given
    val createJson: JValue = service.createServer()
    implicit lazy val formats = org.json4s.DefaultFormats
    val createServer: Server = createJson.extract[Server]
    val startJson: JValue = service.executeCommand(createServer.name,
      "command" -> service.Command.START.toString
    )
    val startServer: Server = startJson.extract[Server]
    val url: String = s"http://localhost:${startServer.port}"

    // when
    val (code: Int, json: JValue) = httpQuery.get(s"$url${Constants.STUB_CONFIGURATION}")

    // then
    assert(code === 200)

    val server: WorkingServerConfigurationResponse = json.extract[WorkingServerConfigurationResponse]
    assert(service.serverNames.contains(server.name) === true)
    assert(server.description === "working server")
    assert(server.api.size === 1)

    val path: Path = server.api.head
    assert(path.method === "GET")
    assert(path.path === "/fake")
    assert(path.scenarios === List(
      new Scenario(
        name = "ok",
        contentType = "application/json",
        body = parse( """{"message": "fake server successful response"}""").extract[Object],
        code = 200,
        headers = Map()
      ), new Scenario(
        name = "validation",
        contentType = "application/json",
        body = parse( """{"error": "example of validation error on fake server"}""").extract[Object],
        code = 400,
        headers = Map()
      ), new Scenario(
        name = "server_error",
        contentType = "application/json",
        body = parse( """{"error": "example of server error on fake server"}""").extract[Object],
        code = 503,
        headers = Map()
      )))
  }

}
