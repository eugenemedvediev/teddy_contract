import java.io.File
import java.nio.file.Files

import com.isightpartners.qa.teddy.Service
import com.isightpartners.qa.teddy.creator.DummyCreator
import com.isightpartners.qa.teddy.db.ESDB
import com.isightpartners.qa.teddy.engine.StubEngine
import com.isightpartners.qa.teddy.model.{Configuration, Server}
import org.apache.commons.io.FileUtils
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
 * iSIGHT Partners, Inc. Proprietary
 */

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/13/15
 */
class RecoveryIntegrationTest extends FunSuite with Payload with BeforeAndAfterAll {

  var service: Service = _
  var elasticData: File = _
  var engine: StubEngine = _

  override protected def beforeAll() = {
    super.beforeAll()
    elasticData = Files.createTempDirectory("elasticsearch_data_recovery_test").toFile
    engine = new StubEngine(DummyCreator, new ESDB(elastic_home = elasticData.getAbsolutePath))
    service = new Service(engine)
  }

  override protected def afterAll() = {
    try {
      FileUtils.forceDelete(elasticData)
    } catch {
      case e: Exception => println("exception during deleting: " + elasticData)
    }

    super.afterAll()
  }

  test("recover started") {
    // given
    val createJson: JValue = service.create()
    implicit lazy val formats = org.json4s.DefaultFormats
    val createServer: Server = createJson.extract[Server]
    var json: JValue = service.executeCommand(createServer.name,
      ("command" -> service.Command.LOAD.toString) ~
        ("configuration" -> parse(DUMMY_CONFIGURATION))
    )
    var server: Server = json.extract[Server]
    assert(engine.serverNames.contains(server.name) === true)
    assert(createServer.name === server.name)
    assert(server.started === true)
    assert(server.api !== Array.empty)
    assert(server.api.length === 2)
    var configuration: Configuration = parse(DUMMY_CONFIGURATION).extract[Configuration]
    assert(server.description === configuration.description)
    assert(server.api === configuration.api)

    // when
    Thread.sleep(5000)
    service = new Service(new StubEngine(DummyCreator, new ESDB(elastic_home = elasticData.getAbsolutePath)))
    json = engine.statusAll

    // then
    val servers = json.extract[List[Server]]
    server = servers.find(p => p.name == server.name).get
    assert(engine.serverNames.contains(server.name) === true)
    assert(createServer.name === server.name)
    assert(server.started === true)
    assert(server.api !== Array.empty)
    assert(server.api.length === 2)
    configuration = parse(DUMMY_CONFIGURATION).extract[Configuration]
    assert(server.description === configuration.description)
    assert(server.api === configuration.api)
  }

}
