package com.isightpartners.qa.teddy.servlet

import com.isightpartners.qa.teddy.creator.DummyCreator
import com.isightpartners.qa.teddy.db.ESDB
import com.typesafe.config.ConfigFactory

class DummyServlet extends StubServlet(creator = DummyCreator,  db = new ESDB(elastic_home = ConfigFactory.load.getString("elastic.home"), "dummy"))