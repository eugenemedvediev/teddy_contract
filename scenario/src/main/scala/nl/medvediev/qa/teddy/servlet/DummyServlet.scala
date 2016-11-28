package nl.medvediev.qa.teddy.servlet

import nl.medvediev.qa.teddy.creator.DummyCreator
import nl.medvediev.qa.teddy.db.ESDB
import com.typesafe.config.ConfigFactory

class DummyServlet extends StubServlet(creator = DummyCreator,  db = new ESDB(elastic_home = ConfigFactory.load.getString("elastic.home"), "dummy"))