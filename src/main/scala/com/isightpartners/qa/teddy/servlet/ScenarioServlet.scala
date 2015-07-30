package com.isightpartners.qa.teddy.servlet

import com.isightpartners.qa.teddy.Service
import com.isightpartners.qa.teddy.creator.ScenarioCreator
import com.isightpartners.qa.teddy.engine.StubEngine

/**
 * Created by ievgen on 18/07/15.
 */
class ScenarioServlet extends StubServlet {
  val service = new Service(new StubEngine(creator = ScenarioCreator))
}
