package nl.medvediev.qa.teddy.servlet

import nl.medvediev.qa.teddy.creator.ScenarioCreator
import nl.medvediev.qa.teddy.db.ESDB
import com.typesafe.config.ConfigFactory

class ScenarioServlet extends StubServlet(creator = ScenarioCreator,  db = new ESDB(elastic_home = ConfigFactory.load.getString("elastic.home"), "scenario"))
