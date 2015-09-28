package qa.dummy

/**
 * Created by ievgen on 23/09/15.
 */
class ContractError(val message: String)

object ContractError {
  val DUMMY_CONFIGURATION = "/_dummy_"
  val CONTRACT_ERROR = s"""{"contract_error": "%s"}"""
  val DUMMY_ERROR = s"""{"internal_dummy_error": "%s"}"""
  val NOT_SUPPORTED_PATH_ERROR = CONTRACT_ERROR.format(s"not supported path or method by contract; check configuration GET $DUMMY_CONFIGURATION")
}