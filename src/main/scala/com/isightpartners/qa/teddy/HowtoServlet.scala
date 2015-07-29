/**
 * iSIGHT Partners, Inc. Proprietary
 */

package com.isightpartners.qa.teddy

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/4/15
 */
class HowtoServlet extends HttpServlet {
  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("application/json")
    response.setCharacterEncoding("UTF-8")
    val body =
      """
        |[
        |{
        | "url":"/servers",
        | "method": "GET",
        | "description":"show info about all working servers"
        |},
        |{
        | "url":"/servers",
        | "method": "POST",
        | "description":"create new working server"
        |},
        |{
        | "url":"/servers/{server_name}",
        | "method": "DELETE",
        | "description":"delete working server"
        |},
        |{
        | "url":"/servers/{server_name}",
        | "method": "GET",
        | "description":"show info about working server"
        |},
        |{
        | "url":"/servers/{server_name}",
        | "method": "PUT",
        | "description":"start working server",
        | "payload": {"command":"start"}
        |},
        |{
        | "url":"/servers/{server_name}",
        | "method": "PUT",
        | "description":"stop working server",
        | "payload": {"command":"stop"}
        |},
        |{
        | "url":"/servers/{server_name}",
        | "method": "PUT",
        | "description":"restore default configuration",
        | "payload": {"command":"clean"}
        |},
        |{
        | "url":"/servers/{server_name}",
        | "method": "PUT",
        | "description":"load specified configuration",
        | "payload": {
        |     "command":"load",
        |     "configuration":{
        |       "description":"Wires server",
        |       "api":[]
        |     }
        |  }
        |}
        |]
        | """.stripMargin
    response.getWriter.write(pretty(parse(body)))
  }
}
