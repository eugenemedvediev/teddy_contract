/**
 * iSIGHT Partners, Inc. Proprietary
 */

package qa.common.model

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/3/15
 */
case class Configuration(description: String = null, api: List[Route] = null)

case class Route(method: String, path: String, scenarios: List[Scenario])

case class Scenario(name: String = "", request: ScenarioRequest = null, response: ScenarioResponse = null)

case class ScenarioRequest(headers: Map[String, String] = Map(), body: Object)

case class ScenarioResponse(headers: Map[String, String] = Map(), body: Object, code: Int)
