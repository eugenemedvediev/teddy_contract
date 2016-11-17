package qa.http

import nl.medvediev.apiserver._

object Methods {

  def aPost = POST(_, _, _)

  def aPut = PUT(_, _, _)

  def aPatch = PATCH(_, _, _)

  def aGet = GET(_, _, _)

  def aDelete = DELETE(_, _, _)

  def aOptions = OPTIONS(_, _, _)

  val methods = Map(
    "POST" -> aPost,
    "PUT" -> aPut,
    "PATCH" -> aPatch,
    "GET" -> aGet
    "DELETE" -> aDelete,
    "OPTIONS" -> aOptions
  )

  def get(method: String) = methods.getOrElse(method, throw new IllegalArgumentException(s"Unsupported method: $method"))

}
