package nl.medvediev.apiserver

import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures
import org.simpleframework.http.Status
import play.api.libs.ws.ning.NingWSClient

/**
  * Created by ievgen on 26/05/16.
  */
class APIServerTest extends FunSuite {

  test("get simple") {
    // given
    val apiRoutes: List[APIRoute] = List(
      GET(
        path = "/",
        params = Map(),
        response = new SimpleAPIResponse(
          code = Status.OK.getCode,
          contentType = "application/json",
          body = """{"test": "ok"}""",
          headers = Map()
        )
      )
    )
    val server: APIServer = new APIServer(9090, apiRoutes)
    server.start
    val wsClient = NingWSClient()

    // when
    val result = wsClient
      .url(s"http://localhost:${server.getPort}")
      .get()

    // then
    ScalaFutures.whenReady(result) {
      response => {
        server.stop()
        assert(response.status === 200)
        assert(response.header("Content-Type").get === "application/json")
        assert(response.body === """{"test": "ok"}""")
      }
    }
  }

  test("get dynamic") {
    // given
    val apiRoutes: List[APIRoute] = List(
      GET(
        path = "/",
        params = Map(),
        response = new DynamicAPIResponse({
          (request, params) => {
            println("Dynamic")
            new SimpleAPIResponse(
              code = Status.OK.getCode,
              contentType = "application/json",
              body = """{"test": "dynamic"}""",
              headers = Map()
            )
          }
        })
      )
    )
    val server: APIServer = new APIServer(9090, apiRoutes)
    server.start
    val wsClient = NingWSClient()

    // when
    val result = wsClient
      .url(s"http://localhost:${server.getPort}")
      .get()

    // then
    ScalaFutures.whenReady(result) {
      response => {
        server.stop()
        assert(response.status === 200)
        assert(response.header("Content-Type").get === "application/json")
        assert(response.body === """{"test": "dynamic"}""")
      }
    }
  }

  test("post dynamic") {
    // given
    val apiRoutes: List[APIRoute] = List(
      POST(
        path = "/",
        params = Map(),
        response = new DynamicAPIResponse({
          (request, params) => {
            new SimpleAPIResponse(
              code = Status.CREATED.getCode,
              contentType = "application/json",
              body = """{"test": "dynamic"}""",
              headers = Map()
            )
          }
        })
      )
    )
    val server: APIServer = new APIServer(9090, apiRoutes)
    server.start
    val wsClient = NingWSClient()

    // when
    val result = wsClient
      .url(s"http://localhost:${server.getPort}")
      .post("test")

    // then
    ScalaFutures.whenReady(result) {
      response => {
        server.stop()
        assert(response.status === 201)
        assert(response.header("Content-Type").get === "application/json")
        assert(response.body === """{"test": "dynamic"}""")
      }
    }
  }

  test("reuse port") {
    // given
    val server1 = new APIServer(9090, List())
    val server2 = new APIServer(9090, List())

    // when
    server1.start
    server2.start

    // then
    assert(server1.getPort >= 9090)
    assert(server2.getPort > 9090)
    assert(server1.getPort != server2.getPort)

    // when
    server1.stop //port free
    server2.stop //port + 1 free

    // then
    assert(server1.getPort === 9090)
    assert(server2.getPort === 9090)
  }
}
