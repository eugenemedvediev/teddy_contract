/**
 * iSIGHT Partners, Inc. Proprietary
 */

import java.io.File
import java.nio.file.Files

import com.isightpartners.qa.teddy.HttpQuery
import com.isightpartners.qa.teddy.creator.{DummyCreator, ScenarioCreator}
import com.isightpartners.qa.teddy.db.ESDB
import com.isightpartners.qa.teddy.model.{Configuration, Path, Server}
import com.isightpartners.qa.teddy.service.StubService
import org.apache.commons.io.FileUtils
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._
import org.scalatest.{BeforeAndAfterEach, FunSuite}

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/10/15
 */
class ScenarioServerIntegrationTest extends FunSuite with ScenarioPayload with BeforeAndAfterEach {

  case class WorkingServerConfigurationResponse(name: String, description: String, api: List[Path])

  val httpQuery = new HttpQuery {}
  var elasticData: File = _
  var service: StubService = _
  var server: Server = _
  var port: Int = _

  override protected def beforeEach() = {
    elasticData = Files.createTempDirectory("elasticsearch_data_recovery_test").toFile
    service = new StubService(ScenarioCreator, new ESDB(elastic_home = elasticData.getAbsolutePath, "test"))
  }

  override protected def afterEach() = {
    try {
      FileUtils.forceDelete(elasticData)
    } catch {
      case e: Exception => println("exception during deleting: " + elasticData)
    }
  }

  test("loaded configuration ok") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val jsonLoad: JValue = service.create(parse(SCENARIO_CONFIGURATION).extract[Configuration])
    port = jsonLoad.extract[Server].port

    val url: String = s"http://localhost:${port}"
    Thread.sleep(5000)

    // when
    val (code: Int, json: JValue) = httpQuery.post(
      s"$url/wires/servers",
      Map(
        "Accept" -> "application/json",
        "Content-Type" -> "application/json",
        "Authorization" -> "Token blablabla"
      ),
      parse(
        """
          |{
          |  "server": "Angel",
          |  "description": "Angel Server Description"
          |}
        """.stripMargin)
    )

    // then
    assert(code === 200)
    val expected: JValue = parse(
      """
        |{
        |  "id": 1,
        |  "server": "Angel",
        |  "description": "Angel Server Description",
        |  "port": 8090
        |}
      """.stripMargin)
    assert(json === expected)

    // and when
    val (code2: Int, json2: JValue) = httpQuery.post(
      s"$url/wires/savedsearches",
      Map(
        "Accept" -> "application/json",
        "Content-Type" -> "application/json",
        "Authorization" -> "Token blablabla"
      ),
      parse(
        """
          |{
          |  "title": "google",
          |  "content": "google today"
          |}""".stripMargin)
    )

    // then
    assert(code2 === 200)
    val expected2: JValue = parse(
      """{
        |  "id": 1,
        |  "title": "google",
        |  "content": "google today"
        |}""".stripMargin)
    assert(json2 === expected2)
  }

  test("configuration page") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val jsonLoad: JValue = service.create(parse(SCENARIO_CONFIGURATION).extract[Configuration])
    port = jsonLoad.extract[Server].port

    val url: String = s"http://localhost:${port}"
    Thread.sleep(5000)

    // when
    val (code: Int, json: JValue) = httpQuery.get(s"$url${DummyCreator.STUB_CONFIGURATION}")

    // then
    assert(code === 200)

    val server1: WorkingServerConfigurationResponse = json.extract[WorkingServerConfigurationResponse]
    assert(service.serverNames.contains(server1.name) === true)
    assert(server1.description === "Scenario Test Configuration")
    assert(server1.api.size === 2)

    val path: Path = server1.api.head
    assert(path.method === "POST")
    assert(path.path === "/wires/servers")
    //    assert(path.scenarios === List(
    //      new Scenario(
    //        name = "ok",
    //        contentType = "application/json",
    //        body = parse( """{"message": "fake server successful response"}""").extract[Object],
    //        code = 200,
    //        headers = Map()
    //      ), new Scenario(
    //        name = "validation",
    //        contentType = "application/json",
    //        body = parse( """{"error": "example of validation error on fake server"}""").extract[Object],
    //        code = 400,
    //        headers = Map()
    //      ), new Scenario(
    //        name = "server_error",
    //        contentType = "application/json",
    //        body = parse( """{"error": "example of server error on fake server"}""").extract[Object],
    //        code = 503,
    //        headers = Map()
    //      )))
  }


}