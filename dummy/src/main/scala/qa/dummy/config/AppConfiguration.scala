package qa.dummy.config

import com.typesafe.config.ConfigFactory
import util.Try

/**
  * Created by ievgen on 11/11/2016.
  */
trait AppConfiguration {
val config = ConfigFactory.load()

  /** Host name/address to start service on. */
  lazy val serviceHost = Try(config.getString("service.host")).getOrElse("localhost")

  /** Port to start service on. */
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)


}
