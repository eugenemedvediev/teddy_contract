package qa.dummy

import org.scalatest.FunSuite
import qa.common.model.{ScenarioResponse, ScenarioRequest, Scenario}

/**
 * Created by ievgen on 23/09/15.
 */
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
    val scenarios: List[Scenario] = List[Scenario](
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

}
