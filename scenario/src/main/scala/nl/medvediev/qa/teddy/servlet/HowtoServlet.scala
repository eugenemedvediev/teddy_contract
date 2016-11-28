/**
 * nl.medvediev.qa
 */

package nl.medvediev.qa.teddy.servlet

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
 *
 * @author Ievgen Medvediev
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
        | "url":"/{dummies|scenarios}",
        | "method": "GET",
        | "description":"show info about all working {dummies|scenarios}"
        |},
        |{
        | "url":"/{dummies|scenarios}",
        | "method": "POST",
        | "description":"create new working server"
        |},
        |{
        | "url":"/{dummies|scenarios}/{server_name}",
        | "method": "DELETE",
        | "description":"delete working server"
        |},
        |{
        | "url":"/{dummies|scenarios}/{server_name}",
        | "method": "GET",
        | "description":"show info about working server"
        |},
        |{
        | "url":"/{dummies|scenarios}/{server_name}",
        | "method": "PUT",
        | "description":"start working server",
        | "payload": {"command":"start"}
        |},
        |{
        | "url":"/{dummies|scenarios}/{server_name}",
        | "method": "PUT",
        | "description":"stop working server",
        | "payload": {"command":"stop"}
        |},
        |{
        | "url":"/{dummies|scenarios}/{server_name}",
        | "method": "PUT",
        | "description":"restore default configuration",
        | "payload": {"command":"clean"}
        |},
        |{
        | "url":"/{dummies|scenarios}/{server_name}",
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
