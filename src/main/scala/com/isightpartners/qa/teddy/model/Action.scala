/**
 * iSIGHT Partners, Inc. Proprietary
 */

package com.isightpartners.qa.teddy.model

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/3/15
 */
case class Action(command: String, configuration: Configuration = null)

case class Configuration(description: String = null, api: List[Path] = null)

case class Path(method: String, path: String, scenarios: List[Scenario])

case class Scenario(name: String = "", contentType: String, body: Object, code: Int, headers: Map[String, String] = Map())
