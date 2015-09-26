import java.net.URL

import fr.simply.StubServer
import org.scalatest.FunSuite
import qa.common.model.Configuration
import qa.dummy.DummyCreator
import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.response.Response


/**
 * Created by ievgen on 24/09/15.
 */
class DummyCreatorIntegrationTest extends FunSuite {
  val DEFAULT_PORT = 8090

  test("test") {
    // given
    val server: StubServer = DummyCreator.createServer(DEFAULT_PORT, new Configuration("test"))
    server.start
    val currentPort = server.portInUse

    // when
    val httpClient = new HttpClient
    val response: Response = httpClient.get(new URL("http://google.nl"))

    // then
    assert(response.status.code == 200)
  }



}
