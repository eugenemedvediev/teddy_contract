package com.isightpartners.qa.teddy

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

/**
 * Created by ievgen on 18/07/15.
 */
class ScenarioServlet extends HttpServlet {

  private val service = new Service


  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("application/json")
    response.setCharacterEncoding("UTF-8")

    val path: String = request.getPathInfo
    val responseBody =
      if (path != null && path.startsWith("/") && !path.equals("/")) {
        response.setStatus(HttpServletResponse.SC_OK)
        val name: String = path.drop(1)
        service.getStatus(name)
      } else {
        response.setStatus(HttpServletResponse.SC_OK)
        service.getStatus
      }
    response.getWriter.write(compact(responseBody))
  }

  override def doPut(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("application/json")
    response.setCharacterEncoding("UTF-8")

    val path: String = request.getPathInfo
    val responseBody: JValue =
      if (path != null && path.startsWith("/")) {
        response.setStatus(HttpServletResponse.SC_OK)
        val name: String = path.drop(1)
        val json: JValue = parse(request.getInputStream)
        service.executeCommand(name, json)
      } else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST)
        "error" -> "server name hasn't been provided"
      }
    response.getWriter.write(compact(responseBody))
  }

  override def doPost(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("application/json")
    response.setCharacterEncoding("UTF-8")

    val path: String = request.getPathInfo
    val responseBody: JValue =
      if (path != null && path.startsWith("/")) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST)
        "error" -> s"not supported path '%s'".format(path)
      } else {
        response.setStatus(HttpServletResponse.SC_OK)
        service.createServer()
      }
    response.getWriter.write(compact(responseBody))
  }

  override def doDelete(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("application/json")
    response.setCharacterEncoding("UTF-8")

    val path = request.getPathInfo
    val responseBody: JValue =
      if (path != null && path.startsWith("/")) {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT)
        val name: String = path.drop(1)
        service.deleteServer(name)
        JObject()
      } else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST)
        "error" -> s"not supported path '%s'".format(path)
      }
    response.getWriter.write(compact(responseBody))
  }

}
