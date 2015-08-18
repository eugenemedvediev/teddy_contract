import com.isightpartners.qa.teddy.db.DB
import com.isightpartners.qa.teddy.model.Configuration

/**
 * Created by ievgen on 18/08/15.
 */
class TestDB extends DB {
  def writeConfiguration(name: String, configuration: Configuration) = {}

  def getAllStartedConfigurations: List[(String, Configuration)] = List[(String, Configuration)]()

  def deleteConfiguration(name: String) = {}

  def setStarted(name: String, started: Boolean) = {}

  def readConfiguration(name: String): Configuration = new Configuration()
}
