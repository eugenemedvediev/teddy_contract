package com.isightpartners.qa.teddy.servlet

import com.isightpartners.qa.teddy.creator.ScenarioCreator
import com.isightpartners.qa.teddy.db.ESDB
import com.typesafe.config.ConfigFactory

class ScenarioServlet extends StubServlet(creator = ScenarioCreator,  db = new ESDB(elastic_home = ConfigFactory.load.getString("elastic.home"), "scenario"))
