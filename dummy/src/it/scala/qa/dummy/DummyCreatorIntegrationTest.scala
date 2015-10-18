package qa.dummy

import java.net.URL

import fr.simply.StubServer
import org.json4s.jackson.JsonMethods._
import org.scalatest.FunSuite
import qa.common.exception.ConfigurationException
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
        |            "body": {
        |              "input": "data"
        |            }
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
    val requestBody = RequestBody("""{"input":"data"}""", MediaType.APPLICATION_JSON)

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

  test("text/plain body") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header",
        |      "scenarios": [
        |        {
        |          "name": "correct required header",
        |          "request": {
        |            "headers": {
        |              "Authorization": "Token 123"
        |            },
        |            "body": null
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "text\/plain"
        |            },
        |            "body": "match header",
        |            "code": 200
        |          }
        |        }
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
    assert(response.body.asString == "match header\n")
  }


  test("not match body") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header",
        |      "scenarios": [
        |        {
        |          "name": "correct required header",
        |          "request": {
        |            "headers": {
        |              "Authorization": "Token 123"
        |            },
        |            "body": {
        |               "some": "body"
        |            }
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "text\/plain"
        |            },
        |            "body": "match header",
        |            "code": 200
        |          }
        |        }
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
    val requestBody = RequestBody("""{"notsome":"body"}""", MediaType.APPLICATION_JSON)

    // when
    val response = httpClient.post(
      new URL(s"$url/test/header"),
      Some(requestBody),
      Headers(Map(
        "Authorization" -> "Token 123"
      ))
    )

    // then
    assert(response.status.code == 503)
    assert((parse(response.body.asString) \\ "contract_error").extract[String] == "no any scenarios with specified body")
  }

  test("failure read from file") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header",
        |      "scenarios": [
        |        {
        |          "name": "correct required header",
        |          "request": {
        |            "headers": {
        |              "Authorization": "Token 123"
        |            },
        |            "body": {
        |               "some": "body"
        |            }
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": "@/tmp/not_existing_file",
        |            "code": 200
        |          }
        |        }
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
    val requestBody = RequestBody("""{"some":"body"}""", MediaType.APPLICATION_JSON)

    // when
    val response = httpClient.post(
      new URL(s"$url/test/header"),
      Some(requestBody),
      Headers(Map(
        "Authorization" -> "Token 123"
      ))
    )

    // then
    assert(response.status.code == 503)
    assert((parse(response.body.asString) \\ "internal_dummy_error").extract[String] == "Can't load body from file: /tmp/not_existing_file")
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

  test("missing route scenarios") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header"
        |    }
        |]
      """.stripMargin).extract[List[Route]]

    // when
    val thrown = intercept[ConfigurationException] {
      DummyCreator.createServer(DEFAULT_PORT, new Configuration("any", api))
    }

    // then
    assert(thrown.getMessage == "Empty scenarios for route: POST /test/header")
  }

  test("missing query param empty") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header",
        |      "scenarios": [
        |        {
        |          "name": "any",
        |          "request": {},
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "message": "match query"
        |            },
        |            "code": 200
        |          }
        |        },
        |        {
        |          "name": "missing header",
        |          "request": {
        |            "query": {
        |              "!query": "some query"
        |            }
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "error": "query parameter is not specified"
        |            },
        |            "code": 400
        |          }
        |        }
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
      Some(requestBody)
    )

    // then
    assert(response.status.code == 400)
    assert((parse(response.body.asString) \\ "error").extract[String] == "query parameter is not specified")
  }

  test("missing query param not fit") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header",
        |      "scenarios": [
        |        {
        |          "name": "any",
        |          "request": {
        |            "query": {
        |              "notfit": "value"
        |            }
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "message": "match query"
        |            },
        |            "code": 200
        |          }
        |        },
        |        {
        |          "name": "missing header",
        |          "request": {
        |            "query": {
        |              "!query": "some query"
        |            }
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "error": "query parameter is not specified"
        |            },
        |            "code": 400
        |          }
        |        }
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
      Some(requestBody)
    )

    // then
    assert(response.status.code == 400)
    assert((parse(response.body.asString) \\ "error").extract[String] == "query parameter is not specified")
  }

  test("query param different value") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header",
        |      "scenarios": [
        |        {
        |          "name": "any",
        |          "request": {
        |            "query": {
        |              "query": "different value"
        |            }
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "message": "match query"
        |            },
        |            "code": 200
        |          }
        |        },
        |        {
        |          "name": "missing header",
        |          "request": {
        |            "query": {
        |              "!query": "some query"
        |            }
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "error": "query parameter is not specified"
        |            },
        |            "code": 400
        |          }
        |        }
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
      Some(requestBody)
    )

    // then
    assert(response.status.code == 400)
    assert((parse(response.body.asString) \\ "error").extract[String] == "query parameter is not specified")
  }

  test("no scenarios with specified query") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header",
        |      "scenarios": [
        |        {
        |          "name": "any",
        |          "request": {
        |            "query": {
        |              "query": "specified value"
        |            }
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "message": "match query"
        |            },
        |            "code": 200
        |          }
        |        }
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
      new URL(s"$url/test/header?query=any+value"),
      Some(requestBody)
    )

    // then
    assert(response.status.code == 503)
    assert((parse(response.body.asString) \\ "contract_error").extract[String] == "no any scenarios with specified query")
  }

  test("query param any value") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header",
        |      "scenarios": [
        |        {
        |          "name": "any",
        |          "request": {
        |            "query": {
        |              "query": "any value"
        |            }
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "message": "match query"
        |            },
        |            "code": 200
        |          }
        |        },
        |        {
        |          "name": "missing header",
        |          "request": {
        |            "query": {
        |              "!query": null
        |            }
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "error": "query parameter is not specified"
        |            },
        |            "code": 400
        |          }
        |        }
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
      new URL(s"$url/test/header?query=any+value"),
      Some(requestBody)
    )

    // then
    println(parse(response.body.asString))
    assert(response.status.code == 200)
    assert((parse(response.body.asString) \\ "message").extract[String] == "match query")
  }

  test("query param exact value") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/header",
        |      "scenarios": [
        |        {
        |          "name": "any",
        |          "request": {
        |            "query": {
        |              "query": "exact value"
        |            }
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "message": "match query"
        |            },
        |            "code": 200
        |          }
        |        },
        |        {
        |          "name": "missing header",
        |          "request": {
        |            "query": {
        |              "!query": "exact value"
        |            }
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "error": "query parameter is not specified"
        |            },
        |            "code": 400
        |          }
        |        }
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
      new URL(s"$url/test/header?query=exact+value"),
      Some(requestBody)
    )

    // then
    assert(response.status.code == 200)
    assert((parse(response.body.asString) \\ "message").extract[String] == "match query")
  }

  test("path params") {
    // given
    implicit lazy val formats = org.json4s.DefaultFormats
    val api: List[Route] = parse(
      """
        |[
        |    {
        |      "method": "POST",
        |      "path": "/test/*/header/*",
        |      "scenarios": [
        |        {
        |          "name": "any",
        |          "request": {
        |          },
        |          "response": {
        |            "headers": {
        |              "Content-Type": "application/json"
        |            },
        |            "body": {
        |              "message": "match path"
        |            },
        |            "code": 200
        |          }
        |        }
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
      new URL(s"$url/test/17/header/33?query=exact+value"),
      Some(requestBody)
    )

    // then
    assert(response.status.code == 200)
    assert((parse(response.body.asString) \\ "message").extract[String] == "match path")
  }

}
