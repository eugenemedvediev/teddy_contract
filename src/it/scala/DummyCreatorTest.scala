import com.isightpartners.qa.teddy.HttpQuery
import com.isightpartners.qa.teddy.creator.DummyCreator
import com.isightpartners.qa.teddy.model.{ScenarioResponse, ScenarioRequest, Scenario, Path}
import fr.simply.StubServer
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._
import org.scalatest.FunSuite
import org.json4s.JsonDSL._

/**
 * Created by ievgen on 09/09/15.
 */
class DummyCreatorTest extends FunSuite with HttpQuery {

  test("multiple required values") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Path] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header",
        |      "scenarios": [
        |                {
        |          "name": "missing header",
        |          "request": {
        |            "headers": {
        |              "!Authorization": "Token 123",
        |              "!Content-Type": "application/json",
        |              "!Accept": "application/json"
        |            },
        |            "body": null
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "error": "Missing header"
        |            },
        |            "code": 400
        |          }
        |        },
        |        {
        |          "name": "correct required header",
        |          "request": {
        |            "headers": {},
        |            "body": null
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "message": "match header"
        |            },
        |            "code": 200
        |          }
        |        }
        |
        |
        |      ]
        |    }
        |]
      """
        .stripMargin).extract[List[Path]]
    val workingServer: StubServer = DummyCreator.createWorkingServer("test", "any", api)
    workingServer.start
    val port = workingServer.portInUse
    val url = s"http://localhost:$port"

    // when
    val (code: Int, json: JValue) = post(
      s"$url/test/header",
      Map(
        "Authorization" -> "Token 123",
        "Content-Type" -> "application/json",
        "Accept" -> "application/json"
      ),
      null
    )

    // then
    assert(code == 200)
    assert((json \\ "message").extract[String] == "match header")
  }

  test("missing header from multiple required values") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Path] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header",
        |      "scenarios": [
        |                {
        |          "name": "missing header",
        |          "request": {
        |            "headers": {
        |              "!Authorization": "Token 123",
        |              "!Content-Type": "application/json",
        |              "!Accept": "application/json"
        |            },
        |            "body": null
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "error": "Not valid @expectedHeader header"
        |            },
        |            "code": 400
        |          }
        |        },
        |        {
        |          "name": "correct required header",
        |          "request": {
        |            "headers": {},
        |            "body": null
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "message": "match header"
        |            },
        |            "code": 200
        |          }
        |        }
        |
        |
        |      ]
        |    }
        |]
      """
        .stripMargin).extract[List[Path]]
    val workingServer: StubServer = DummyCreator.createWorkingServer("test", "any", api)
    workingServer.start
    val port = workingServer.portInUse
    val url = s"http://localhost:$port"

    // when
    val (code: Int, json: JValue) = post(
      s"$url/test/header",
      Map(
        "Authorization" -> "Token 123",
        "Content-Type" -> "application/json"
      ),
      null
    )

    // then
    assert(code == 400)
    assert((json \\ "error").extract[String] == "Not valid Accept header")
  }

  test("missing header") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Path] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header",
        |      "scenarios": [
        |                {
        |          "name": "missing header",
        |          "request": {
        |            "headers": {
        |              "!Authorization": null
        |            },
        |            "body": null
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "error": "Authorization header is missing"
        |            },
        |            "code": 400
        |          }
        |        }
        |
        |      ]
        |    }
        |]
      """
        .stripMargin).extract[List[Path]]
    val workingServer: StubServer = DummyCreator.createWorkingServer("test", "any", api)
    workingServer.start
    val port = workingServer.portInUse
    val url = s"http://localhost:$port"

    // when
    val (code: Int, json: JValue) = post(
      s"$url/test/header",
      Map(
        "Accept" -> "application/json"
      ),
      parse("{}")
    )

    // then
    assert(code == 400)
    assert((json \\ "error").extract[String] == "Authorization header is missing")
  }

  test("header with not expected value") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Path] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header",
        |      "scenarios": [
        |                {
        |          "name": "missing header",
        |          "request": {
        |            "headers": {
        |              "!Authorization": "Token 123"
        |            },
        |            "body": null
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "error": "Incorrect token"
        |            },
        |            "code": 400
        |          }
        |        }
        |
        |      ]
        |    }
        |]
      """
        .stripMargin).extract[List[Path]]
    val workingServer: StubServer = DummyCreator.createWorkingServer("test", "any", api)
    workingServer.start
    val port = workingServer.portInUse
    val url = s"http://localhost:$port"

    // when
    val (code: Int, json: JValue) = post(
      s"$url/test/header",
      Map(
        "Authorization" -> "Token not123"
      ),
      parse("{}")
    )

    // then
    assert(code == 400)
    assert((json \\ "error").extract[String] == "Incorrect token")
  }

  test("correct required header with value") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Path] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header",
        |      "scenarios": [
        |                {
        |          "name": "missing header",
        |          "request": {
        |            "headers": {
        |              "!Authorization": "Token 123"
        |            },
        |            "body": null
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "error": "Incorrect token"
        |            },
        |            "code": 400
        |          }
        |        },
        |                        {
        |          "name": "correct required header",
        |          "request": {
        |            "headers": {
        |              "Authorization": "Token 123"
        |            },
        |            "body": null
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "message": "match header"
        |            },
        |            "code": 200
        |          }
        |        }
        |
        |
        |      ]
        |    }
        |]
      """
        .stripMargin).extract[List[Path]]
    val workingServer: StubServer = DummyCreator.createWorkingServer("test", "any", api)
    workingServer.start
    val port = workingServer.portInUse
    val url = s"http://localhost:$port"

    // when
    val (code: Int, json: JValue) = post(
      s"$url/test/header",
      Map(
        "Authorization" -> "Token 123"
      ),
      null
    )

    // then
    assert(code == 200)
    assert((json \\ "message").extract[String] == "match header")
  }

  test("correct required header without value") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Path] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header",
        |      "scenarios": [
        |                {
        |          "name": "missing header",
        |          "request": {
        |            "headers": {
        |              "!Authorization": null
        |            },
        |            "body": null
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "error": "Incorrect token"
        |            },
        |            "code": 400
        |          }
        |        },
        |                        {
        |          "name": "correct required header",
        |          "request": {
        |            "headers": {
        |              "Authorization": "Token 123"
        |            },
        |            "body": null
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "message": "match header"
        |            },
        |            "code": 200
        |          }
        |        }
        |
        |
        |      ]
        |    }
        |]
      """
        .stripMargin).extract[List[Path]]
    val workingServer: StubServer = DummyCreator.createWorkingServer("test", "any", api)
    workingServer.start
    val port = workingServer.portInUse
    val url = s"http://localhost:$port"

    // when
    val (code: Int, json: JValue) = post(
      s"$url/test/header",
      Map(
        "Authorization" -> "Token 123"
      ),
      null
    )

    // then
    assert(code == 200)
    assert((json \\ "message").extract[String] == "match header")
  }

}
