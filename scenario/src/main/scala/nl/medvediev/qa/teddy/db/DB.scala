/**
 * nl.medvediev.qa
 */

package nl.medvediev.qa.teddy.db

import nl.medvediev.qa.teddy.model.Configuration

/**
 *
 * @author Ievgen Medvediev
 * @since 4/13/15
 */
trait DB {
  def writeConfiguration(name: String, configuration: Configuration)

  def setStarted(name: String, started: Boolean)

  def readConfiguration(name: String): Configuration

  def deleteConfiguration(name: String)

  def getAllStartedConfigurations: List[(String, Configuration)]
}
