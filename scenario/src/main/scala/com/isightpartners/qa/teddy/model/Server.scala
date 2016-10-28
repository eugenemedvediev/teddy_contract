/**
 * iSIGHT Partners, Inc. Proprietary
 */

package com.isightpartners.qa.teddy.model

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/3/15
 */
case class Server(name: String, port: Int, started:Boolean, description: String, api: List[Path])
