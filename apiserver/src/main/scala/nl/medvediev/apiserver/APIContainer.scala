package nl.medvediev.apiserver

import java.nio.charset.Charset

import nl.medvediev.pathmatcher.PathMatcher
import org.simpleframework.http.core.Container
import org.simpleframework.http.{Request, Response, Status}

/**
  * Created by ievgen.medvediev on 26/05/16.
  */
class APIContainer(defaultResponse: SimpleAPIResponse, routes: List[APIRoute]) extends Container {

  override def handle(request: Request, response: Response): Unit = {

    if (!hasRoute(request, response)) {
      response.setCode(defaultResponse.code)
      response.setContentType(defaultResponse.contentType)
      response.getPrintStream.println(defaultResponse.body)
    }
    response.getPrintStream.close()
    response.close()
  }

  private def hasRoute(request: Request, response: Response): Boolean = {
    routes.exists {
      route => route.response match {
        case simple: SimpleAPIResponse => processRequest(request, response, route)({
          (response, patterns) => simple.asInstanceOf[SimpleAPIResponse]
        })
        case dynamic: DynamicAPIResponse => processRequest(request, response, route)({
          (response, patterns) => dynamic.asInstanceOf[DynamicAPIResponse].response(request, patterns)
        })
      }
    }
  }

  private def processRequest(request: Request, response: Response, route: APIRoute)(transformResponse: (APIResponse, List[(String, String)]) => SimpleAPIResponse): Boolean = {
    val matcherResult = matchRoute(request, route)
    if (matcherResult.isMatch) {
      val simpleAPIResponse = transformResponse(route.response, matcherResult.patterns)
      makeResponse(response, simpleAPIResponse)
    }
    matcherResult.isMatch
  }

  private def matchRoute(request: Request, apiRoute: APIRoute): PathMatcher#MatcherResult = {
    if (request.getMethod.equalsIgnoreCase(apiRoute.verb.toString) && testParams(request, apiRoute.params))
      PathMatcher().matches(request.getPath.getPath, apiRoute.path)
    else
      PathMatcher().MatcherResult(isMatch = false)
  }

  private def makeResponse(response: Response, simpleAPIResponse: SimpleAPIResponse) {
    response.setCode(simpleAPIResponse.code)
    response.setContentType(simpleAPIResponse.contentType.toString)
    simpleAPIResponse.headers.foreach { case (k, v) => response.setValue(k, v) }
    response.getPrintStream.write(simpleAPIResponse.body.getBytes(Charset.forName("UTF-8")))
  }

  private def testParams(request: Request, params: Map[String, String]): Boolean =
    params.forall { case (key, value) => request.getParameter(key) != null && request.getParameter(key) == value }

}