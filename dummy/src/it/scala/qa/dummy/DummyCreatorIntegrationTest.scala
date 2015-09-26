package qa.dummy

import java.net.URL

import fr.simply.StubServer
import org.json4s.jackson.JsonMethods._
import org.scalatest.FunSuite
import qa.common.model.{Configuration, Route}
import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.header.{Headers, MediaType}
import uk.co.bigbeeconsultants.http.request.RequestBody


/**
 * Created by ievgen on 24/09/15.
 */
class DummyCreatorIntegrationTest extends FunSuite {
  val DEFAULT_PORT = 8090

  test("multiple required values") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
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
        .stripMargin).extract[List[Route]]
    val workingServer: StubServer = DummyCreator.createServer(DEFAULT_PORT, new Configuration("any", api))
    workingServer.start
    val port = workingServer.portInUse
    val url = s"http://localhost:$port"
    val httpClient = new HttpClient
    val requestBody = RequestBody("", MediaType.APPLICATION_JSON)

    // when
    val response = httpClient.post(
      new URL(s"$url/test/header"),
      Some(requestBody),
      Headers(Map(
        "Authorization" -> "Token 123",
        "Content-Type" -> "application/json",
        "Accept" -> "application/json"
      ))
    )

    // then
    assert(response.status.code == 200)
    assert(response.body.isTextual == true)
    assert((parse(response.body.asString) \\ "message").extract[String] == "match header")
  }

  test("missing header from multiple required values") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
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
        .stripMargin).extract[List[Route]]
    val workingServer: StubServer = DummyCreator.createServer(DEFAULT_PORT, new Configuration("any", api))
    workingServer.start
    val port = workingServer.portInUse
    val url = s"http://localhost:$port"
    val httpClient = new HttpClient
    val requestBody = RequestBody("", MediaType.APPLICATION_JSON)

    // when
    val response = httpClient.post(
      new URL(s"$url/test/header"),
      Some(requestBody),
      Headers(Map(
        "Authorization" -> "Token 123",
        "Content-Type" -> "application/json"
      ))
    )

    // then
    assert(response.status.code == 400)
    assert((parse(response.body.asString) \\ "error").extract[String] == "Not valid Accept header")
  }

  test("missing header") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
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
        .stripMargin).extract[List[Route]]
    val workingServer: StubServer = DummyCreator.createServer(DEFAULT_PORT, new Configuration("any", api))
    workingServer.start
    val port = workingServer.portInUse
    val url = s"http://localhost:$port"
    val httpClient = new HttpClient
    val requestBody = RequestBody("{}", MediaType.APPLICATION_JSON)

    // when
    val response = httpClient.post(
      new URL(s"$url/test/header"),
      Some(requestBody),
      Headers(Map(
        "Accept" -> "application/json"
      ))
    )

    // then
    assert(response.status.code == 400)
    assert((parse(response.body.asString) \\ "error").extract[String] == "Authorization header is missing")
  }

  test("header with not expected value") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
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
        .stripMargin).extract[List[Route]]
    val workingServer: StubServer = DummyCreator.createServer(DEFAULT_PORT, new Configuration("any", api))
    workingServer.start
    val port = workingServer.portInUse
    val url = s"http://localhost:$port"
    val httpClient = new HttpClient
    val requestBody = RequestBody("{}", MediaType.APPLICATION_JSON)

    // when
    val response = httpClient.post(
      new URL(s"$url/test/header"),
      Some(requestBody),
      Headers(Map(
        "Authorization" -> "Token not123"
      ))
    )

    // then
    assert(response.status.code == 400)
    assert((parse(response.body.asString) \\ "error").extract[String] == "Incorrect token")
  }

  test("correct required header with value") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
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
        .stripMargin).extract[List[Route]]
    val workingServer: StubServer = DummyCreator.createServer(DEFAULT_PORT, new Configuration("any", api))
    workingServer.start
    val port = workingServer.portInUse
    val url = s"http://localhost:$port"
    val httpClient = new HttpClient
    val requestBody = RequestBody("", MediaType.APPLICATION_JSON)

    // when
    val response = httpClient.post(
      new URL(s"$url/test/header"),
      Some(requestBody),
      Headers(Map(
        "Authorization" -> "Token 123"
      ))
    )

    // then
    assert(response.status.code == 200)
    assert((parse(response.body.asString) \\ "message").extract[String] == "match header")
  }

  test("correct required header without value") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
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
        .stripMargin).extract[List[Route]]
    val workingServer: StubServer = DummyCreator.createServer(DEFAULT_PORT, new Configuration("any", api))
    workingServer.start
    val port = workingServer.portInUse
    val url = s"http://localhost:$port"
    val httpClient = new HttpClient
    val requestBody = RequestBody("", MediaType.APPLICATION_JSON)

    // when
    val response = httpClient.post(
      new URL(s"$url/test/header"),
      Some(requestBody),
      Headers(Map(
        "Authorization" -> "Token 123"
      ))
    )

    // then
    assert(response.status.code == 200)
    assert((parse(response.body.asString) \\ "message").extract[String] == "match header")
  }


}
