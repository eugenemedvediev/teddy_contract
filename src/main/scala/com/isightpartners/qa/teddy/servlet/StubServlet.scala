package com.isightpartners.qa.teddy.servlet

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import com.isightpartners.qa.teddy.creator.{Creator, DummyCreator}
import com.isightpartners.qa.teddy.db.DB
import com.isightpartners.qa.teddy.service.{StubService, Service}
import com.isightpartners.qa.teddy.model.Configuration
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._

class StubServlet(creator: Creator, db: DB) extends HttpServlet {

  val service: Service = new StubService(creator, db)

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("application/json")
    response.setCharacterEncoding("UTF-8")

    val path: String = request.getPathInfo
    val responseBody =
      if (path != null && path.startsWith("/") && !path.equals("/")) {
        response.setStatus(HttpServletResponse.SC_OK)
        val name: String = path.drop(1)
        service.status(name)
      } else {
        response.setStatus(HttpServletResponse.SC_OK)
        service.statusAll()
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
        implicit val formats = DefaultFormats
        val configuration = json.extract[Configuration]
        service.update(name, configuration)
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
        val json: JValue = parse(request.getInputStream)
        implicit val formats = DefaultFormats
        val configuration = json.extract[Configuration]
        service.create(configuration)
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
        service.delete(name)
        JObject()
      } else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST)
        "error" -> s"not supported path '%s'".format(path)
      }
    response.getWriter.write(compact(responseBody))
  }

}
