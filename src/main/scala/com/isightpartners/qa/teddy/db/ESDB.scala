/**
 * iSIGHT Partners, Inc. Proprietary
 */

package com.isightpartners.qa.teddy.db

import java.util

import com.isightpartners.qa.teddy.model.{Configuration, Path}
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mapping.FieldType.StringType
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.common.settings.ImmutableSettings
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

/**
 *
 * @author Ievgen Medvediev (imedvediev@isightpartners.com)
 * @since 4/10/15
 */
class ESDB(val elastic_home: String) extends DB {

  val settings = ImmutableSettings.settingsBuilder()
    .put("protocol", "http")
    .put("path.home", elastic_home).build()
  val client = ElasticClient.local(settings)
  //  val settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch-ievgen").build()
  //  val client = ElasticClient.remote(settings, ("localhost", 9300))

  def writeConfiguration(name: String, configuration: Configuration) = {
    implicit val formats = DefaultFormats
    client.execute {
      index into "stub/configurations" id name fields(
        "description" -> configuration.description,
        "api" -> Serialization.write(configuration.api),
        "started" -> true
        )
    }
  }

  def setStarted(name: String, started: Boolean) = {
    client.execute {
      index into "stub/configurations" id name fields (
        "started" -> started
        )
    }
  }

  def readConfiguration(name: String): Configuration = {
    //TODO: not used anymore
    implicit val formats = DefaultFormats
    val execute: Future[GetResponse] = client.execute {
      get id name from "stub/configurations"
    }
    val source: util.Map[String, AnyRef] = Await.result(execute, 120 seconds).getSource
    val description: String = source.get("description").toString
    val api: String = source.get("api").toString
    new Configuration(description, parse(api).extract[List[Path]])
  }

  def deleteConfiguration(name: String) = {
    client.execute {
      delete id name from "stub/configurations"
    }
  }

  def getAllStartedConfigurations: List[(String, Configuration)] = {
    try {
      val existsFeature: Future[IndicesExistsResponse] = client.exists("stub")
      val result: IndicesExistsResponse = Await.result(existsFeature, 10 second)
      if (result.isExists) {
        val execute: Future[SearchResponse] = client.execute {
          search in "stub" -> "configurations" query {
            term("started", true)
          } sort (by field "_id")
        }
        val searchResult: SearchResponse = Await.result(execute, 10 second)
        implicit lazy val formats = org.json4s.DefaultFormats
        println(searchResult.getHits.getHits)
        searchResult.getHits.getHits.toList.map(p => (p.getId, new Configuration(p.getSource.get("description").toString, parse(p.getSource.get("api").toString).extract[List[Path]])))
      } else {
        client.execute {
          create index "stub" shards 5 mappings (
            "configurations" as(
              "description" typed StringType,
              "api" typed StringType
              )
            )
        }
        List[(String, Configuration)]()
      }
    } catch {
      case _: org.elasticsearch.transport.RemoteTransportException => List[(String, Configuration)]()
      case e: Throwable => println("hmm, problem is here"); throw e
    }
  }
}
