package nl.medvediev.apiserver

import java.io.IOException
import java.net.InetSocketAddress

import org.simpleframework.http.Request
import org.simpleframework.http.core.ContainerSocketProcessor
import org.simpleframework.transport.connect.SocketConnection

/**
  * Created by ievgen.medvediev on 26/05/16.
  */

case class APIRoute(verb: Verb, path: String, params: Map[String, String] = Map(), response: APIResponse)

sealed trait APIResponse

case class SimpleAPIResponse(code: Int, contentType: String, body: String, headers: Map[String, String] = Map()) extends APIResponse

case class DynamicAPIResponse(response: (Request, List[(String, String)]) => SimpleAPIResponse) extends APIResponse

object GET {
  def apply(path: String, params: Map[String, String], response: APIResponse): APIRoute =
    APIRoute(GetVerb, path, params, response)
}

object POST {
  def apply(path: String, params: Map[String, String], response: APIResponse): APIRoute =
    APIRoute(PostVerb, path, params, response)
}

class APIServer(initPort: Int, routes: List[APIRoute]) {
  private var defaultResponse = SimpleAPIResponse(404, "application/json", "not found")


  private var socketConnection: SocketConnection = _
  private var port = initPort

  def getPort: Int = port

  def start: APIServer = {
    this.socketConnection = startServer()
    this
  }

  def stop() = {
    if (socketConnection != null) socketConnection.close()
    port = initPort
  }

  def defaultResponse(code: Int, contentType: String, body: String): APIServer = {
    this.defaultResponse = SimpleAPIResponse(code, contentType, body)
    this
  }

  private def startServer(): SocketConnection = {
    def connect(connection: SocketConnection, port: Int): SocketConnection = {
      val socketAddress = new InetSocketAddress(port)
      try {
        connection.connect(socketAddress)
        this.port = socketAddress.getPort
        connection
      } catch {
        case e: IOException => connect(connection, port + 1)
      }
    }

    val container = new APIContainer(defaultResponse, routes)
    val connection = new SocketConnection(new ContainerSocketProcessor(container))
    connect(connection, port)
  }

}
