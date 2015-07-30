package com.isightpartners.qa.teddy.creator

import fr.simply.{ServerRoute, StubServer}
import org.json4s.JValue

/**
 * Created by ievgen on 30/07/15.
 */
object ScenarioCreator extends Creator {
  override val defaultAPISettingsKey: String = "working.server.api"
  override def generateServerRouteList(x: JValue): List[ServerRoute] = List[ServerRoute]()
}
