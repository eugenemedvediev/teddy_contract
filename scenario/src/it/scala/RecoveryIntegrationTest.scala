import java.io.File
import java.nio.file.Files

import com.isightpartners.qa.teddy.creator.DummyCreator
import com.isightpartners.qa.teddy.db.ESDB
import com.isightpartners.qa.teddy.service.StubService
import com.isightpartners.qa.teddy.model.{Configuration, Server}
import org.json4s.JsonAST.JValue
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
class RecoveryIntegrationTest extends FunSuite with DummyPayload with BeforeAndAfterAll {

  var service: StubService = _
  var elasticData: File = _

  override protected def beforeAll() = {
    super.beforeAll()
    elasticData = Files.createTempDirectory("elasticsearch_data_recovery_test").toFile
    service = new StubService(DummyCreator, new ESDB(elastic_home = elasticData.getAbsolutePath, "test"))
  }

  override protected def afterAll() = {
    try {

    } catch {
      case e: Exception => println("exception during deleting: " + elasticData)
    }

    super.afterAll()
  }

  test("recover started") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val configuration: Configuration = parse(DUMMY_FULL_CONFIGURATION).extract[Configuration]
    var json: JValue = service.create(configuration)
    val server: Server = json.extract[Server]
    assert(service.serverNames.contains(server.name) === true)
    assert(server.started === true)
    assert(server.api !== Array.empty)
    assert(server.api.length === 2)
    assert(server.description === configuration.description)
    assert(server.api === configuration.api)

    // when
    Thread.sleep(5000)
    service = new StubService(DummyCreator, new ESDB(elastic_home = elasticData.getAbsolutePath, "test"))
    json = service.statusAll

    // then
    val servers = json.extract[List[Server]]
    val checkServer = servers.find(p => p.name == server.name).get
    assert(service.serverNames.contains(server.name) === true)
    assert(checkServer.name === server.name)
    assert(checkServer.started === true)
    assert(checkServer !== Array.empty)
    assert(checkServer.api.length === 2)
    assert(checkServer.description === configuration.description)
    assert(checkServer.api === configuration.api)
  }

}
