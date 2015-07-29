/**
 * iSIGHT Partners, Inc. Proprietary
 */

package com.isightpartners.qa.teddy.db

import com.isightpartners.qa.teddy.model.Configuration

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/13/15
 */
trait DB {
  def writeConfiguration(name: String, configuration: Configuration)

  def setStarted(name: String, started: Boolean)

  def readConfiguration(name: String): Configuration

  def deleteConfiguration(name: String)

  def getAllStartedConfigurations: List[(String, Configuration)]
}
