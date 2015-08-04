/**
 * iSIGHT Partners, Inc. Proprietary
 */

import java.io.File
import java.nio.file.{Files, Path => FilePath}

import com.isightpartners.qa.teddy.HttpQuery
import com.isightpartners.qa.teddy.creator.DummyCreator
import com.isightpartners.qa.teddy.db.ESDB
import com.isightpartners.qa.teddy.service.StubService
import com.isightpartners.qa.teddy.model.{Configuration, Path, Server}
import org.apache.commons.io.FileUtils
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.scalatest.{BeforeAndAfterEach, FunSuite}

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/10/15
 */
class WorkingServerIntegrationTest extends FunSuite with Payload with BeforeAndAfterEach {

  case class WorkingServerConfigurationResponse(name: String, description: String, api: List[Path])

  val httpQuery = new HttpQuery {}
  var elasticData: File = _
  var service: StubService = _
  var server: Server = _
  var port: Int = _

  override protected def beforeEach() = {
    //    super.beforeAll()
    elasticData = Files.createTempDirectory("elasticsearch_data_recovery_test").toFile
    service = new StubService(DummyCreator, new ESDB(elastic_home = elasticData.getAbsolutePath))
  }

  override protected def afterEach() = {
    try {
      FileUtils.forceDelete(elasticData)
    } catch {
      case e: Exception => println("exception during deleting: " + elasticData)
    }

  }

  test("loaded configuration ok scenario 1") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val jsonLoad: JValue = service.create(parse(DUMMY_FULL_CONFIGURATION).extract[Configuration])
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
        """{
          |            "server": "Angel",
          |            "description": "Angel Server Description"
          |          }""".stripMargin)
    )

    // then
    assert(code === 200)
    val expected: JValue = parse(
      """{
        |            "id": 1,
        |            "server": "Angel",
        |            "description": "Angel Server Description",
        |            "port": 8090
        |          }
      """.stripMargin)
    assert(json === expected)
  }

  test("load configuration ok scenario 2") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val jsonLoad: JValue = service.create(parse(DUMMY_FULL_CONFIGURATION).extract[Configuration])
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
        """{
          |            "description": "Angel Server Description"
          |          }""".stripMargin)
    )

    // then
    assert(code === 400)
    val expected: JValue = parse(
      """{
        |  "error": "Field 'server' is required"
         }""".stripMargin)
    assert(json === expected)
  }

  test("load configuration not found header") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val jsonLoad: JValue = service.create(parse(DUMMY_FULL_CONFIGURATION).extract[Configuration])
    port = jsonLoad.extract[Server].port

    val url: String = s"http://localhost:${port}"
    Thread.sleep(5000)

    // when
    val (code: Int, json: JValue) = httpQuery.post(
      s"$url/wires/servers",
      Map(
        "Authorization" -> "Token notexisting"
      ),
      parse(
        """{
          |            "description": "Angel Server Description"
          |          }""".stripMargin)
    )

    // then
    assert(code === 503)
    val expected: JValue = parse(
      """{
        |  "contract_error":"no any scenarios with specified header"}
         }""".stripMargin)
    assert(json === expected)
  }

  test("load configuration not found empty header") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val jsonLoad: JValue = service.create(parse(DUMMY_FULL_CONFIGURATION).extract[Configuration])
    port = jsonLoad.extract[Server].port

    val url: String = s"http://localhost:${port}"
    Thread.sleep(5000)

    // when
    val (code: Int, json: JValue) = httpQuery.post(
      s"$url/wires/servers",
      Map(),
      parse(
        """{
          |            "description": "Angel Server Description"
          |          }""".stripMargin)
    )

    // then
    assert(code === 503)
    val expected: JValue = parse(
      """{
        |  "contract_error":"no any scenarios with specified header"}
         }""".stripMargin)
    assert(json === expected)
  }

  test("load configuration not found body") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val jsonLoad: JValue = service.create(parse(DUMMY_FULL_CONFIGURATION).extract[Configuration])
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
        """{
          |            "server": "Angel"
          |          }""".stripMargin)
    )

    // then
    assert(code === 503)
    val expected: JValue = parse(
      """{
        |  "contract_error":"no any scenarios with specified body"}
         }""".stripMargin)
    assert(json === expected)
  }

  test("load configuration not found path") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val jsonLoad: JValue = service.create(parse(DUMMY_FULL_CONFIGURATION).extract[Configuration])
    port = jsonLoad.extract[Server].port

    val url: String = s"http://localhost:${port}"
    Thread.sleep(5000)

    // when
    val (code: Int, json: JValue) = httpQuery.get(s"$url/fake_929dskfs0ewkl")

    // then
    assert(code === 404)
    val expected: JValue = "contract_error" -> "not supported path or method by contract; check configuration GET /stub/configuration"
    assert(json === expected)
  }

  test("configuration page") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val jsonLoad: JValue = service.create(parse(DUMMY_FULL_CONFIGURATION).extract[Configuration])
    port = jsonLoad.extract[Server].port

    val url: String = s"http://localhost:${port}"
    Thread.sleep(5000)

    // when
    val (code: Int, json: JValue) = httpQuery.get(s"$url${DummyCreator.STUB_CONFIGURATION}")

    // then
    assert(code === 200)

    val server1: WorkingServerConfigurationResponse = json.extract[WorkingServerConfigurationResponse]
    assert(service.serverNames.contains(server1.name) === true)
    assert(server1.description === "Dummy Test Configuration")
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

  test("loaded configuration no header") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val jsonLoad: JValue = service.create(parse(DUMMY_NO_HEADER_CONFIGURATION).extract[Configuration])
    port = jsonLoad.extract[Server].port

    val url: String = s"http://localhost:${port}"
    Thread.sleep(5000)

    // when
    val (code: Int, json: JValue) = httpQuery.post(
      s"$url/wires/servers",
      Map(),
      parse(
        """{
          |            "server": "Angel",
          |            "description": "Angel Server Description"
          |          }""".stripMargin)
    )

    // then
    assert(code === 200)
    val expected: JValue = parse(
      """{
        |            "id": 1,
        |            "server": "Angel",
        |            "description": "Angel Server Description",
        |            "port": 8090
        |          }
      """.stripMargin)
    assert(json === expected)
  }

  test("loaded configuration no body") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val jsonLoad: JValue = service.create(parse(DUMMY_NO_BODY_CONFIGURATION).extract[Configuration])
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
        """{}""".stripMargin)
    )

    // then
    assert(code === 200)
    val expected: JValue = parse(
      """{
        |            "id": 1,
        |            "server": "Angel",
        |            "description": "Angel Server Description",
        |            "port": 8090
        |          }
      """.stripMargin)
    assert(json === expected)
  }

}
