package qa.dummy.config

import com.typesafe.config.ConfigFactory
import util.Try

trait AppConfiguration {
  val config = ConfigFactory.load()

  lazy val interface = Try(config.getString("service.interface")).getOrElse("0.0.0.0")

  lazy val port = Try(config.getInt("service.port")).getOrElse(8000)


}
