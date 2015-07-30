/**
 * iSIGHT Partners, Inc. Proprietary
 */

package com.isightpartners.qa.teddy.servlet

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/3/15
 */

import com.isightpartners.qa.teddy.Service
import com.isightpartners.qa.teddy.creator.DummyCreator
import com.isightpartners.qa.teddy.engine.StubEngine

class DummyServlet extends StubServlet {
  val service = new Service(new StubEngine(DummyCreator))
}
