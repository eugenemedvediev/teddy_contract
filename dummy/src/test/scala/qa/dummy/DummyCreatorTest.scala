package qa.dummy

import org.json4s.JsonAST.JValue
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.scalatest.FunSuite
import qa.common.model.{Scenario, ScenarioRequest, ScenarioResponse}

class DummyCreatorTest extends FunSuite {

  test("getScenariosWithSpecifiedHeaders empty") {
    // given
    val scenarios: List[Scenario] = List[Scenario]()
    val requestHeaders: Map[String, String] = Map[String, String]()

    // when
    val result: List[Scenario] = DummyCreator.getScenariosWithSpecifiedHeaders(scenarios, requestHeaders)

    // then
    assert(result.isEmpty)
  }

  test("getScenariosWithSpecifiedHeaders scenarios null") {
    // given
    val scenarios: List[Scenario] = null
    val requestHeaders: Map[String, String] = Map[String, String]("Accept" -> "applicatoin/json")

    // when
    val result: List[Scenario] = DummyCreator.getScenariosWithSpecifiedHeaders(scenarios, requestHeaders)

    // then
    assert(result.isEmpty)
  }

  test("getScenariosWithSpecifiedHeaders headers null") {
    // given
    val scenarios: List[Scenario] = List(
      new Scenario(
        "name",
        new ScenarioRequest(body = null),
        new ScenarioResponse(body = null, code = 200)
      )
    )
    val requestHeaders: Map[String, String] = null

    // when
    val result: List[Scenario] = DummyCreator.getScenariosWithSpecifiedHeaders(scenarios, requestHeaders)

    // then
    assert(result.length == 1)
  }

  test("getScenariosWithSpecifiedHeaders headers null not match request headers") {
    // given
    val scenarios: List[Scenario] = List[Scenario](
      new Scenario(
        "name",
        new ScenarioRequest(headers = Map("Accept" -> "application/json"), body = null),
        new ScenarioResponse(body = null, code = 200)
      )
    )
    val requestHeaders: Map[String, String] = null

    // when
    val result: List[Scenario] = DummyCreator.getScenariosWithSpecifiedHeaders(scenarios, requestHeaders)

    // then
    assert(result.isEmpty)
  }

  test("getScenariosWithSpecifiedHeaders headers empty not match request headers") {
    // given
    val scenarios: List[Scenario] = List[Scenario](
      new Scenario(
        "name",
        new ScenarioRequest(headers = Map("Accept" -> "application/json"), body = null),
        new ScenarioResponse(body = null, code = 200)
      )
    )
    val requestHeaders: Map[String, String] = Map()

    // when
    val result: List[Scenario] = DummyCreator.getScenariosWithSpecifiedHeaders(scenarios, requestHeaders)

    // then
    assert(result.isEmpty)
  }

  test("getScenariosWithSpecifiedHeaders match") {
    // given
    val scenarios: List[Scenario] = List[Scenario](
      new Scenario(
        "name",
        new ScenarioRequest(headers = Map("Accept" -> "application/json"), body = null),
        new ScenarioResponse(body = null, code = 200)
      )
    )
    val requestHeaders: Map[String, String] = Map("Accept" -> "application/json")

    // when
    val result: List[Scenario] = DummyCreator.getScenariosWithSpecifiedHeaders(scenarios, requestHeaders)

    // then
    assert(result.length == 1)
  }

  test("getScenariosWithMissingRequiredHeaders empty") {
    // given
    val scenarios: List[Scenario] = List[Scenario]()
    val requestHeaders: Map[String, String] = Map[String, String]()

    // when
    val result: List[Scenario] = DummyCreator.getScenariosWithMissingRequiredHeaders(scenarios, requestHeaders)

    // then
    assert(result.isEmpty)
  }

  test("getScenariosWithMissingRequiredHeaders scenarios null") {
    // given
    val scenarios: List[Scenario] = null
    val requestHeaders: Map[String, String] = Map[String, String]()

    // when
    val result: List[Scenario] = DummyCreator.getScenariosWithMissingRequiredHeaders(scenarios, requestHeaders)

    // then
    assert(result.isEmpty)
  }

  test("getScenariosWithMissingRequiredHeaders headers null") {
    // given
    val scenarios: List[Scenario] = List(new Scenario(
      "name",
      new ScenarioRequest(body = null),
      new ScenarioResponse(body = null, code = 200)
    ))
    val requestHeaders: Map[String, String] = null

    // when
    val result: List[Scenario] = DummyCreator.getScenariosWithMissingRequiredHeaders(scenarios, requestHeaders)

    // then
    assert(result.isEmpty)
  }

  test("getScenariosWithMissingRequiredHeaders headers null with missing required request header any value") {
    // given
    val scenarios: List[Scenario] = List(new Scenario(
      "name",
      new ScenarioRequest(headers = Map("!Accept" -> null), body = null),
      new ScenarioResponse(body = null, code = 200)
    ))
    val requestHeaders: Map[String, String] = null

    // when
    val result: List[Scenario] = DummyCreator.getScenariosWithMissingRequiredHeaders(scenarios, requestHeaders)

    // then
    assert(result.length == 1)
  }

  test("getScenariosWithMissingRequiredHeaders headers null with missing required request header exact value") {
    // given
    val scenarios: List[Scenario] = List(new Scenario(
      "name",
      new ScenarioRequest(headers = Map("!Accept" -> "application/json"), body = null),
      new ScenarioResponse(body = null, code = 200)
    ))
    val requestHeaders: Map[String, String] = null

    // when
    val result: List[Scenario] = DummyCreator.getScenariosWithMissingRequiredHeaders(scenarios, requestHeaders)

    // then
    assert(result.length == 1)
  }

  test("getScenariosWithMissingRequiredHeaders with required request header any value") {
    // given
    val scenarios: List[Scenario] = List(new Scenario(
      "name",
      new ScenarioRequest(headers = Map("!Accept" -> null), body = null),
      new ScenarioResponse(body = null, code = 200)
    ))
    val requestHeaders: Map[String, String] = Map("Accept" -> "anything")

    // when
    val result: List[Scenario] = DummyCreator.getScenariosWithMissingRequiredHeaders(scenarios, requestHeaders)

    // then
    assert(result.isEmpty)
  }

  test("getScenariosWithMissingRequiredHeaders with missing required request header exact value") {
    // given
    val scenarios: List[Scenario] = List(new Scenario(
      "name",
      new ScenarioRequest(headers = Map("!Accept" -> "application/json"), body = null),
      new ScenarioResponse(body = null, code = 200)
    ))
    val requestHeaders: Map[String, String] = Map("Accept" -> "application/json")

    // when
    val result: List[Scenario] = DummyCreator.getScenariosWithMissingRequiredHeaders(scenarios, requestHeaders)

    // then
    assert(result.isEmpty)
  }

  test("parseBody null") {
    // given

    // when
    val result: String = DummyCreator.parseBody(null)

    // then
    assert(result == null)
  }

  test("parseBody String") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val body: JValue = parse(Serialization.write(new String("yoo")))

    // when
    val result: String = DummyCreator.parseBody(body)

    // then
    assert(result == "\"yoo\"")
  }

  test("parseBody Map") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val body: JValue = parse(Serialization.write(Map("key" -> "value")))

    // when
    val result: String = DummyCreator.parseBody(body)

    // then
    assert(result == """{"key":"value"}""")
  }

  test("parseBody List") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val body: JValue = parse(Serialization.write(List("one", "two")))

    // when
    val result: String = DummyCreator.parseBody(body)

    // then
    assert(result == """["one","two"]""")
  }

  test("parseBody inner file reference") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val path: String = getClass.getClassLoader.getResource("inner_body.json").getPath
    val body: JValue = parse(Serialization.write(s"@$path"))

    // when
    val result: String = DummyCreator.parseBody(body)

    // then
    assert(result == """{"name":"test","address":{"city":"Haarlem"}}""")
  }

  test("parseBody not existing inner file reference") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val path: String = getClass.getClassLoader.getResource(".").getPath
    val body: JValue = parse(Serialization.write(s"@${path}not_existing_file.json"))

    // when
    val thrown = intercept[IllegalArgumentException] {
      DummyCreator.parseBody(body)
    }

    // then
    assert(thrown.getMessage === s"Can't load body from file: ${path}not_existing_file.json")
  }

}
