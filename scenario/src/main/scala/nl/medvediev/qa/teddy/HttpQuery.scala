/**
 * nl.medvediev.qa
 */

package nl.medvediev.qa.teddy

import java.io.InputStream

import nl.medvediev.qa.teddy.model.{Path, Server}
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet, HttpPost, HttpRequestBase}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
 *
 * @author Ievgen Medvediev
 * @since 3/31/15
 */
trait HttpQuery {

  val jsonProperties = Map(
    "Accept" -> "application/json",
    "Content-Type" -> "application/json"
  )

  def get(url: String): (Int, JValue) = {
    val get = new HttpGet(url)
    setProperties(get, jsonProperties)
    try {
      val response = HttpClients.createDefault().execute(get)
      val statusCode: Int = response.getStatusLine.getStatusCode
      val content: JValue = getContent(response)
      (statusCode, content)
    } catch {
      case _: Throwable => (0, JObject())
    }
  }

  def post(url: String, headers: Map[String, String], body: JValue): (Int, JValue) = {
    val post = new HttpPost(url)
    setProperties(post, headers)
    post.setEntity(new StringEntity(compact(body)))
    try {
      val response = HttpClients.createDefault().execute(post)
      val statusCode: Int = response.getStatusLine.getStatusCode
      val content: JValue = getContent(response)
      (statusCode, content)
    } catch {
      case _: Throwable => (0, JObject())
    }
  }

  def ping(url: String, name: String, port: Int): JValue = {
    implicit lazy val formats = org.json4s.DefaultFormats
    val get = new HttpGet(url)
    setProperties(get, jsonProperties)
    try {
      val response = HttpClients.createDefault().execute(get)
      val statusCode: Int = response.getStatusLine.getStatusCode
      val content: JValue = getContent(response)
      val responseName: String = (content \ "name").extract[String]
      val responseDescription: String = (content \ "description").extract[String]
      val responseAPI: List[Path] = (content \ "api").extract[Array[Path]].toList
      if (statusCode == 200 && name.equals(responseName)) {
        Extraction.decompose(new Server(name, port, true, responseDescription, responseAPI))
      } else Extraction.decompose(new Server(name, port, false, null, null))
    } catch {
      case _: Throwable => Extraction.decompose(new Server(name, port, false, null, null))
    }
  }

  def setProperties(request: HttpRequestBase, properties: Map[String, String]) = properties.foreach {
    case (name, value) => request.setHeader(name, value)
  }

  def getContent(response: CloseableHttpResponse): JValue = {
    if (response.getEntity != null && response.getEntity.getContent != null) {
      val content: InputStream = response.getEntity.getContent
      try {
        val parsed: JValue = parse(content)
        content.close()
        response.close()
        parsed
      } catch {
        case _: Throwable => JObject()
      }
    } else {
      response.close()
      JObject()
    }
  }


}
