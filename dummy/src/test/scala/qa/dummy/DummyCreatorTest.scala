package qa.dummy

import org.json4s.JsonAST.JValue
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.scalatest.FunSuite
import qa.common.exception.{ContractException, ConfigurationException}
import qa.common.model.{Route, Scenario, ScenarioRequest, ScenarioResponse}

class DummyCreatorTest extends FunSuite {

  test("filterScenariosByBody: scenarios: null, content: null") {
    // given
    val scenarios = null
    val content = null

    // when
    val thrown = intercept[IllegalArgumentException](
    	DummyCreator.filterScenariosByBody(scenarios, content)
    )

    // then
    assert(thrown.getMessage === "requirement failed: scenarios are absent")
  }

  test("filterScenariosByBody: scenarios: empty, content: null") {
    // given
    val scenarios = List[Scenario]()
    val content = null

    // when
    val thrown = intercept[IllegalArgumentException](
    	DummyCreator.filterScenariosByBody(scenarios, content)
    )

    // then
    assert(thrown.getMessage === "requirement failed: scenarios are empty")
  }

  test("filterScenariosByBody: scenarios: nonEmpty(no body), content: null") {
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val content = null

    // when
    val list = DummyCreator.filterScenariosByBody(scenarios, content)

    // then
    assert(list.size === 1)
  }

  test("filterScenariosByBody: scenarios: nonEmpty(body), content: null") {
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(body =
        parse("""{"key": "value"}""")
      ),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val content = null

    // when
    val thrown = intercept[ContractException](
    	DummyCreator.filterScenariosByBody(scenarios, content)
    )

    // then
    assert(thrown.getMessage === "no any scenarios with specified body")
  }

  test("filterScenariosByBody: scenarios: nonEmpty(no body), content: empty") {
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val content = "\"\""

    // when
    val list = DummyCreator.filterScenariosByBody(scenarios, content)

    // then
    assert(list.size === 1)
  }

  test("filterScenariosByBody: scenarios: nonEmpty(body), content: empty") {
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(body =
        parse("""{"key": "value"}""")
      ),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val content = ""

    // when
    val thrown = intercept[ContractException](
    	DummyCreator.filterScenariosByBody(scenarios, content)
    )

    // then
    assert(thrown.getMessage === "no any scenarios with specified body")
  }

  test("filterScenariosByBody: scenarios: nonEmpty(no body), content: nonEmpty") {
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val content = """{"key": "value"}"""

    // when
    val list = DummyCreator.filterScenariosByBody(scenarios, content)

    // then
    assert(list.size === 1)
  }

  test("filterScenariosByBody: scenarios: nonEmpty(body), content: nonEmpty don't fit") {
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(body =
        parse("""{"key": "value"}""")
      ),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val content = """{"key": "value2"}"""

    // when
    val thrown = intercept[ContractException](
    	DummyCreator.filterScenariosByBody(scenarios, content)
    )

    // then
    assert(thrown.getMessage === "no any scenarios with specified body")
  }

  test("filterScenariosByBody: scenarios: nonEmpty(body), content: nonEmpty fit one") {
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(body =
        parse("""{"key": "value"}""")
      ),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val content = """{"key": "value"}"""

    // when
    val list = DummyCreator.filterScenariosByBody(scenarios, content)

    // then
    assert(list.size === 1)
  }

  test("filterScenariosByBody: scenarios: nonEmpty(body), content: nonEmpty fit multi") {
    val scenario1 = new Scenario(
      "name",
      new ScenarioRequest(body =
        parse("""{"key": "value"}""")
      ),
      new ScenarioResponse(code = 200)
    )
    val scenario2 = new Scenario(
      "name",
      new ScenarioRequest(body =
        parse("""{"key": "value"}""")
      ),
      new ScenarioResponse(code = 200)
    )
    val scenario3 = new Scenario(
      "name",
      new ScenarioRequest(body =
        parse("""{"key": "value don't fit"}""")
      ),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario1, scenario2, scenario3)
    val content = """{"key": "value"}"""

    // when
    val list = DummyCreator.filterScenariosByBody(scenarios, content)

    // then
    assert(list.size === 2)
  }

  test("filterScenariosByQuery scenarios: null; query: null") {
    // given
    val scenarios = null
    val query = null

    // when
    val thrown = intercept[IllegalArgumentException](
    	DummyCreator.filterScenariosByQuery(scenarios, query)
    )

    // then
    assert(thrown.getMessage === "requirement failed: scenarios are absent")
  }

  test("filterScenariosByQuery scenarios: empty; query: null") {
    // given
    val scenarios = List[Scenario]()
    val query = null

    // when
    val thrown = intercept[IllegalArgumentException](
    	DummyCreator.filterScenariosByQuery(scenarios, query)
    )

    // then
    assert(thrown.getMessage === "requirement failed: scenarios are empty")
  }

  test("filterScenariosByQuery scenarios: nonEmpty(no query); query: null") {
    // given
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val query = null

    // when
    val list = DummyCreator.filterScenariosByQuery(scenarios, query)

    // then
    assert(list.size === 1)
  }

  test("filterScenariosByQuery scenarios: nonEmpty(query); query: null") {
    // given
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(query = Map[String, String](
      	"param" -> "value"
      )),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val query = null

    // when
    val thrown = intercept[ContractException](
    	DummyCreator.filterScenariosByQuery(scenarios, query)
    )

    // then
    assert(thrown.getMessage === "no any scenarios with specified query")
  }

  test("filterScenariosByQuery scenarios: nonEmpty(no query); query: empty") {
    // given
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val query = Map[String, String]()

    // when
    val list = DummyCreator.filterScenariosByQuery(scenarios, query)

    // then
    assert(list.size === 1)
  }

  test("filterScenariosByQuery scenarios: nonEmpty(query); query: empty") {
    // given
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(query = Map[String, String](
      	"param" -> "value"
      )),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val query = Map[String, String]()

    // when
    val thrown = intercept[ContractException](
    	DummyCreator.filterScenariosByQuery(scenarios, query)
    )

    // then
    assert(thrown.getMessage === "no any scenarios with specified query")
  }

  test("filterScenariosByQuery scenarios: nonEmpty(no query); query: nonEmpty") {
    // given
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val query = Map[String, String]("param" -> "value")

    // when
    val list = DummyCreator.filterScenariosByQuery(scenarios, query)

    // then
    assert(list.size === 1)
  }

  test("filterScenariosByQuery scenarios: nonEmpty(query); query: nonEmpty don't fit") {
    // given
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(query = Map[String, String](
      	"param" -> "value2"
      )),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val query = Map[String, String]("param" -> "value")

    // when
    val thrown = intercept[ContractException](
    	DummyCreator.filterScenariosByQuery(scenarios, query)
    )

    // then
    assert(thrown.getMessage === "no any scenarios with specified query")
  }

  test("filterScenariosByQuery scenarios: nonEmpty(query); query: nonEmpty fit one") {
    // given
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(query = Map[String, String](
      	"param" -> "value"
      )),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val query = Map[String, String]("param" -> "value")

    // when
    val list = DummyCreator.filterScenariosByQuery(scenarios, query)

    // then
    assert(list.size === 1)
  }

  test("filterScenariosByQuery scenarios: nonEmpty(query); query: nonEmpty fit multi") {
    // given
    val scenario1 = new Scenario(
      "name",
      new ScenarioRequest(query = Map[String, String](
      	"param" -> "value"
      )),
      new ScenarioResponse(code = 200)
    )
    val scenario2 = new Scenario(
      "name",
      new ScenarioRequest(query = Map[String, String](
      	"param" -> "value"
      )),
      new ScenarioResponse(code = 200)
    )
    val scenario3 = new Scenario(
      "name",
      new ScenarioRequest(query = Map[String, String](
      	"param" -> "value different"
      )),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario1, scenario2, scenario3)
    val query = Map[String, String]("param" -> "value")

    // when
    val list = DummyCreator.filterScenariosByQuery(scenarios, query)

    // then
    assert(list.size === 2)
  }

  test("filterScenariosByHeaders headers empty") {
    // given
    val scenario = new Scenario("ok", new ScenarioRequest(), new ScenarioResponse(code = 200))
    val scenarios: List[Scenario] = List[Scenario](scenario)
    val headers: Map[String, String] = Map[String, String]()

    // when
    val list = DummyCreator.filterScenariosByHeaders(scenarios, headers)

    // then
    assert(list.size == 1)
  }

  test("filterScenariosByHeaders headers null") {
    // given
    val scenario = new Scenario("ok", new ScenarioRequest(), new ScenarioResponse(code = 200))
    val scenarios: List[Scenario] = List[Scenario](scenario)
    val headers: Map[String, String] = null

    // when
    val list = DummyCreator.filterScenariosByHeaders(scenarios, headers)

    // then
    assert(list.size == 1)
  }

  test("filterScenariosByHeaders headers null not match request headers") {
    // given
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(headers = Map("Accept" -> "application/json"), body = null),
      new ScenarioResponse(body = null, code = 200)
    )
    val scenarios: List[Scenario] = List[Scenario](scenario)
    val requestHeaders: Map[String, String] = null

    // when
    val thrown = intercept[ContractException]{
      DummyCreator.filterScenariosByHeaders(scenarios, requestHeaders)
    }

    // then
    assert(thrown.getMessage == "no any scenarios with specified header")
  }

  test("filterScenariosByHeaders headers empty not match request headers") {
    // given
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(headers = Map("Accept" -> "application/json"), body = null),
      new ScenarioResponse(body = null, code = 200)
    )
    val scenarios: List[Scenario] = List[Scenario](scenario)
    val requestHeaders: Map[String, String] = Map()

    // when
    val thrown = intercept[ContractException]{
      DummyCreator.filterScenariosByHeaders(scenarios, requestHeaders)
    }

    // then
    assert(thrown.getMessage == "no any scenarios with specified header")
  }

  test("filterScenariosByHeaders match") {
    // given
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(headers = Map("Accept" -> "application/json"), body = null),
      new ScenarioResponse(body = null, code = 200)
    )
    val scenarios: List[Scenario] = List[Scenario](scenario)
    val requestHeaders: Map[String, String] = Map("Accept" -> "application/json")

    // when
    val result: List[Scenario] = DummyCreator.filterScenariosByHeaders(scenarios, requestHeaders)

    // then
    assert(result.length == 1)
  }

  test("findRequiredScenario headers null") {
    // given
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(),
      new ScenarioResponse(code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val requestHeaders: Map[String, String] = null

    // when
    val result = DummyCreator.findRequiredScenario(scenarios, requestHeaders)

    // then
    assert(result == null)
  }

  test("findRequiredScenario headers null with missing required request header any value") {
    // given
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(headers = Map("!Accept" -> null), body = null),
      new ScenarioResponse(body = null, code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val requestHeaders: Map[String, String] = null

    // when
    val result = DummyCreator.findRequiredScenario(scenarios, requestHeaders)

    // then
    assert(result == scenario)
  }

  test("findRequiredScenario headers null with missing required request header exact value") {
    // given
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(headers = Map("!Accept" -> "application/json"), body = null),
      new ScenarioResponse(body = null, code = 200)
    )
    val scenarios: List[Scenario] = List[Scenario](scenario)
    val requestHeaders: Map[String, String] = null

    // when
    val result = DummyCreator.findRequiredScenario(scenarios, requestHeaders)

    // then
    assert(result == scenario)
  }

  test("findRequiredScenario with required request header any value") {
    // given
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(headers = Map("!Accept" -> null), body = null),
      new ScenarioResponse(body = null, code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val requestHeaders: Map[String, String] = Map("Accept" -> "anything")

    // when
    val result = DummyCreator.findRequiredScenario(scenarios, requestHeaders)

    // then
    assert(result == null)
  }

  test("findRequiredScenario with missing required request header exact value") {
    // given
    val scenario = new Scenario(
      "name",
      new ScenarioRequest(headers = Map("!Accept" -> "application/json"), body = null),
      new ScenarioResponse(body = null, code = 200)
    )
    val scenarios = List[Scenario](scenario)
    val requestHeaders: Map[String, String] = Map("Accept" -> "application/json")

    // when
    val result = DummyCreator.findRequiredScenario(scenarios, requestHeaders)

    // then
    assert(result == null)
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

  test("validateRoute invalid path empty") {
    // given
    val route = new Route("", "", List[Scenario]())

    // when
    val thrown = intercept[ConfigurationException] {
      DummyCreator.validateRoute(route)
    }

    // then
    assert(thrown.getMessage === s"""Not valid path: """"")
  }

  test("validateRoute invalid path starts not from \\") {
    // given
    val route = new Route("", "somepath", List[Scenario]())

    // when
    val thrown = intercept[ConfigurationException] {
      DummyCreator.validateRoute(route)
    }

    // then
    assert(thrown.getMessage === s"""Not valid path: "somepath"""")
  }

  test("validateRoute valid path starts from **") {
    val scenario = new Scenario("ok", new ScenarioRequest(), new ScenarioResponse(code = 200))
    // given
    val route = new Route("GET", "**/", List[Scenario](scenario))

    // when
    DummyCreator.validateRoute(route)
  }

  test("validateRoute invalid method empty") {
    // given
    val route = new Route("", "/", List[Scenario]())

    // when
    val thrown = intercept[ConfigurationException] {
      DummyCreator.validateRoute(route)
    }

    // then
    assert(thrown.getMessage === s"""Not valid method "" in path: "/"""")
  }

  test("validateRoute invalid method not supported") {
    // given
    val route = new Route("NOT_SUPPORTED", "/", List[Scenario]())

    // when
    val thrown = intercept[ConfigurationException] {
      DummyCreator.validateRoute(route)
    }

    // then
    assert(thrown.getMessage === s"""Not valid method "NOT_SUPPORTED" in path: "/"""")
  }

  test("validateRoute empty scenarios") {
    // given
    val route = new Route("GET", "/", List[Scenario]())

    // when
    val thrown = intercept[ConfigurationException] {
      DummyCreator.validateRoute(route)
    }

    // then
    assert(thrown.getMessage === s"""Empty scenarios for route: GET /""")
  }

  test("validateRoute") {
    val scenario = new Scenario("ok", new ScenarioRequest(), new ScenarioResponse(code = 200))
    // given
    val route = new Route("GET", "/", List[Scenario](scenario))

    // when
    DummyCreator.validateRoute(route)
  }
}
