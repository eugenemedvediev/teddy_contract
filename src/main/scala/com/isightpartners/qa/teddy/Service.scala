/**
 * iSIGHT Partners, Inc. Proprietary
 */

package com.isightpartners.qa.teddy

import com.isightpartners.qa.teddy.engine.Engine
import com.isightpartners.qa.teddy.model.Action
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._
import org.json4s._

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/3/15
 */
class Service(engine: Engine) {

  def create(): JValue = {
    engine.create()
  }

  def delete(name: String): Unit = {
    engine.delete(name)
  }

  def status(name: String): JValue = {
    engine.status(name)
  }

  def statusAll(): JValue = {
    engine.statusAll()
  }

  object Command extends Enumeration {
    val LOAD = Value("load")
    val START = Value("start")
    val STOP = Value("stop")
    val CLEAN = Value("clean")
  }

  def executeCommand(name: String, parsed: JValue): JValue = {
    implicit val formats = DefaultFormats
    val body = parsed.extract[Action]
    try {
      Command.withName(body.command) match {
        case Command.START =>
          engine.start(name)
        case Command.STOP =>
          engine.stop(name)
        case Command.LOAD =>
          engine.load(name, body.configuration)
        case Command.CLEAN =>
          engine.clean(name)
      }
    } catch {
      case _: Throwable => "message" -> s"not supported operation '${body.command}'"
    }
  }

}
