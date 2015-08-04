package com.isightpartners.qa.teddy.creator

import com.isightpartners.qa.teddy.model.Path
import fr.simply.{ServerRoute, StubServer}
import org.json4s.JValue

/**
 * Created by ievgen on 30/07/15.
 */
object ScenarioCreator extends Creator {
  override def createServerRoutes(list: List[Path]): List[ServerRoute] = throw new NotImplementedError()
}
