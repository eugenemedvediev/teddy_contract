/**
 * nl.medvediev.qa
 */

package nl.medvediev.qa.teddy.model

/**
 *
 * @author Ievgen Medvediev
 * @since 4/3/15
 */
case class Server(name: String, port: Int, started:Boolean, description: String, api: List[Path])
