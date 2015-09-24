/**
 * iSIGHT Partners, Inc. Proprietary
 */

package qa.common.model

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/3/15
 */
case class Configuration(description: String, api: List[Route] = List[Route]())

case class Route(method: String, path: String, scenarios: List[Scenario] = List[Scenario]())

case class Scenario(name: String, request: ScenarioRequest, response: ScenarioResponse)

case class ScenarioRequest(headers: Map[String, String] = Map(), body: Object = null)

case class ScenarioResponse(headers: Map[String, String] = Map(), body: Object = null, code: Int)
