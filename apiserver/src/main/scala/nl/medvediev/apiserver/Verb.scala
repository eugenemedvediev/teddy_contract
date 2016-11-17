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

case object PutVerb extends Verb {
  override def toString: String = "PUT"
}

case object PatchVerb extends Verb {
  override def toString: String = "PATCH"
}

case object DeleteVerb extends Verb {
  override def toString: String = "DELETE"
}

case object OptionsVerb extends Verb {
  override def toString: String = "OPTIONS"
}

