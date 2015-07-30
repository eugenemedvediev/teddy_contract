/**
 * iSIGHT Partners, Inc. Proprietary
 */

import com.isightpartners.qa.teddy.Service
import com.isightpartners.qa.teddy.creator.DummyCreator
import com.isightpartners.qa.teddy.db.DB
import com.isightpartners.qa.teddy.engine.StubEngine
import com.isightpartners.qa.teddy.model.{Configuration, Server}
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.scalatest.FunSuite

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/7/15
 */
class ServiceIntegrationTest extends FunSuite with Payload {

  private val engine: StubEngine = new StubEngine(DummyCreator, new DB {
    def writeConfiguration(name: String, configuration: Configuration) = {}

    def getAllStartedConfigurations: List[(String, Configuration)] = List[(String, Configuration)]()

    def deleteConfiguration(name: String) = {}

    def setStarted(name: String, started: Boolean) = {}

    def readConfiguration(name: String): Configuration = new Configuration()
  })

  val service: Service = new Service(engine)

  test("create server") {
    // given

    // when
    val json: JValue = engine.create()

    // then
    implicit lazy val formats = org.json4s.DefaultFormats
    val server: Server = json.extract[Server]
    assert(engine.serverNames.contains(server.name) === true)
    assert(server.started === false)
    assert(server.description === null)
    assert(server.api === Array.empty)
  }

  test("delete server") {
    // given
    val createJson: JValue = engine.create()
    implicit lazy val formats = org.json4s.DefaultFormats
    val createServer: Server = createJson.extract[Server]
    var status: List[Server] = engine.statusAll.extract[Array[Server]].toList
    assert(status.count(s => s.name.equals(createServer.name)) === 1)

    // when
    engine.delete(createServer.name)

    // then
    status = engine.statusAll.extract[Array[Server]].toList
    assert(status.count(s => s.name.equals(createServer.name)) === 0)
  }

  test("start server") {
    // given
    val createJson: JValue = engine.create()
    implicit lazy val formats = org.json4s.DefaultFormats
    val createServer: Server = createJson.extract[Server]

    // when
    val json: JValue = service.executeCommand(createServer.name,
      "command" -> service.Command.START.toString
    )

    // then
    val server: Server = json.extract[Server]
    assert(engine.serverNames.contains(server.name) === true)
    assert(createServer.name === server.name)
    assert(server.started === true)
    assert(server.description === "working server")
    assert(server.api !== Array.empty)
  }

  test("stop server") {
    // given
    val createJson: JValue = engine.create()
    implicit lazy val formats = org.json4s.DefaultFormats
    val createServer: Server = createJson.extract[Server]
    service.executeCommand(createServer.name,
      "command" -> service.Command.START.toString
    )

    // when
    val json: JValue = service.executeCommand(createServer.name,
      "command" -> service.Command.STOP.toString
    )

    // then
    val server: Server = json.extract[Server]
    assert(engine.serverNames.contains(server.name) === true)
    assert(createServer.name === server.name)
    assert(server.started === false)
    assert(server.description === null)
    assert(server.api === Array.empty)
  }

  test("load server") {
    // given
    val createJson: JValue = engine.create()
    implicit lazy val formats = org.json4s.DefaultFormats
    val createServer: Server = createJson.extract[Server]

    // when
    service.executeCommand(createServer.name,
      ("command" -> service.Command.LOAD.toString) ~
        ("configuration" -> parse(DUMMY_CONFIGURATION))
    )

    // then
    val server: Server = engine.status(createServer.name).extract[Server]
    assert(engine.serverNames.contains(server.name) === true)
    assert(createServer.name === server.name)
    assert(server.started === true)
    assert(server.api !== Array.empty)
    assert(server.api.length === 2)
    val configuration: Configuration = parse(DUMMY_CONFIGURATION).extract[Configuration]
    assert(server.description === configuration.description)
    assert(server.api === configuration.api)
  }

  test("clean server") {
    // given
    val createJson: JValue = engine.create()
    implicit lazy val formats = org.json4s.DefaultFormats
    val createServer: Server = createJson.extract[Server]
    service.executeCommand(createServer.name,
      ("command" -> service.Command.LOAD.toString) ~
        ("configuration" -> parse(DUMMY_CONFIGURATION))
    )

    // when
    val json: JValue = service.executeCommand(createServer.name,
      "command" -> service.Command.CLEAN.toString
    )

    // then
    val server: Server = json.extract[Server]
    assert(engine.serverNames.contains(server.name) === true)
    assert(createServer.name === server.name)
    assert(server.started === true)
    assert(server.api !== Array.empty)
    assert(server.api.length === 1)
    val configuration: Configuration = parse(DUMMY_CONFIGURATION).extract[Configuration]
    assert(server.description !== configuration.description)
    assert(server.api !== configuration.api)
  }

}
