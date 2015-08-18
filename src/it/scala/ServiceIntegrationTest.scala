/**
 * iSIGHT Partners, Inc. Proprietary
 */

import com.isightpartners.qa.teddy.creator.DummyCreator
import com.isightpartners.qa.teddy.model.{Configuration, Server}
import com.isightpartners.qa.teddy.service.StubService
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._
import org.scalatest.FunSuite

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/7/15
 */
class ServiceIntegrationTest extends FunSuite with DummyPayload {

  private val service: StubService = new StubService(DummyCreator, new TestDB)

  test("create server") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val configuration: Configuration = parse(DUMMY_CONFIGURATION).extract[Configuration]

    // when
    val json: JValue = service.create(configuration)

    // then
    val server: Server = json.extract[Server]
    assert(service.serverNames.contains(server.name) === true)
    assert(server.started === true)
    assert(server.description === "Wires Submission + Search")
    assert(server.api !== Array.empty)
  }

  test("delete server") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val configuration: Configuration = parse(DUMMY_CONFIGURATION).extract[Configuration]
    val createJson: JValue = service.create(configuration)
    val createServer: Server = createJson.extract[Server]
    var status: List[Server] = service.statusAll.extract[Array[Server]].toList
    assert(status.count(s => s.name.equals(createServer.name)) === 1)

    // when
    service.delete(createServer.name)

    // then
    status = service.statusAll.extract[Array[Server]].toList
    assert(status.count(s => s.name.equals(createServer.name)) === 0)
  }

  test("load server") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val configuration: Configuration = parse(DUMMY_CONFIGURATION).extract[Configuration]
    val createJson: JValue = service.create(configuration)
    val createServer: Server = createJson.extract[Server]

    // when
    val configurationUpdate: Configuration = parse(DUMMY_FULL_CONFIGURATION).extract[Configuration]
    service.update(createServer.name, configurationUpdate)

    // then
    val server: Server = service.status(createServer.name).extract[Server]
    assert(service.serverNames.contains(server.name) === true)
    assert(createServer.name === server.name)
    assert(server.started === true)
    assert(server.api !== Array.empty)
    assert(server.api.length === 2)

    assert(server.description === configurationUpdate.description)
    assert(server.api === configurationUpdate.api)
  }

}
