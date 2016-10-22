package nl.medvediev.apiserver

/**
  * Created by ievgen on 26/05/16.
  */
trait Verb {
  def toString: String
}

case object GetVerb extends Verb {
  override def toString: String = "GET"
}

case object PostVerb extends Verb {
  override def toString: String = "POST"
}

