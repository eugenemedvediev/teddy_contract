package qa.dummy.db

import java.util

import akka.event.slf4j.SLF4JLogging
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType.StringType
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.common.settings.ImmutableSettings
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import qa.common.model.{Configuration, Route}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

/**
  *
  * @author Ievgen Medvediev
  * @since 4/10/15
  */
class ESDB(val elastic_home: String) extends DB with SLF4JLogging{
  private val maxQuerySize = 1000
  val settings = ImmutableSettings.settingsBuilder()
    .put("protocol", "http")
    .put("path.home", elastic_home).build()
  val client = ElasticClient.local(settings)
  Thread.sleep(5000)

  //  val settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch-ievgen").build()
  //  val client = ElasticClient.remote(settings, ("localhost", 9300))

  def writeConfiguration(name: String, configuration: Configuration) = {
    implicit val formats = DefaultFormats
    client.execute {
      index into "contract/configurations" id name fields(
        "description" -> configuration.description,
        "api" -> Serialization.write(configuration.api),
        "started" -> true
      )
    }
  }

  def setStarted(name: String, started: Boolean) = {
    client.execute {
      index into "contract/configurations" id name fields (
        "started" -> started
        )
    }
  }

  def readConfiguration(name: String): Configuration = {
    //TODO: not used anymore
    implicit val formats = DefaultFormats
    val execute: Future[GetResponse] = client.execute {
      get id name from "contract/configurations"
    }
    val source: util.Map[String, AnyRef] = Await.result(execute, 120 seconds).getSource
    val description: String = source.get("description").toString
    val api: String = source.get("api").toString
    new Configuration(description, parse(api).extract[List[Route]])
  }

  def deleteConfiguration(name: String) = {
    client.execute {
      delete id name from "contract/configurations"
    }
  }

  def getAllStartedConfigurations: List[(String, Configuration)] = {
    try {
      val existsFeature: Future[IndicesExistsResponse] = client.execute { index exists "contract" }
      val result: IndicesExistsResponse = Await.result(existsFeature, 20 second)
      if (result.isExists) {
        val execute: Future[SearchResponse] = client.execute {
          search in "contract" -> "configurations" size maxQuerySize query {
            termQuery("started", true)
          } sort (by field "_id")
        }
        val searchResult: SearchResponse = Await.result(execute, 20 second)
        implicit lazy val formats = org.json4s.DefaultFormats
        searchResult.getHits.getHits.toList.map(p => (p.getId, Configuration(p.getSource.get("description").toString, parse(p.getSource.get("api").toString).extract[List[Route]])))
      } else {
        client.execute {
          create index "contract" shards 5 mappings (
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

