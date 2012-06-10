package models

import chc._
import com.traackr.scalastic.elasticsearch.Indexer
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.facet.FacetBuilders._
import org.elasticsearch.index.query.QueryBuilders._
import play.api.libs.json.Json._

object SearchModel {
  
  val ticketIndex = "tickets"
  val ticketType = "ticket"
  val ticketMapping = """
  {
    "ticket": {
      "properties": {
        "project_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "project_name": {
          "type": "string",
          "index": "not_analyzed"
        },
        "priority_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "priority_name": {
          "type": "string",
          "index": "not_analyzed"
        },
        "resolution_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "resolution_name": {
          "type": "string",
          "index": "not_analyzed"
        },
        "proposed_resolution_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "proposed_resolution_name": {
          "type": "string",
          "index": "not_analyzed"
        },
        "reporter_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "reporter_name": {
          "type": "string",
          "index": "not_analyzed"
        },
        "severity_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "severity_name": {
          "type": "string",
          "index": "not_analyzed"
        },
        "status_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "status_name": {
          "type": "string",
          "index": "not_analyzed"
        },
        "type_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "type_id": {
          "type": "string",
          "index": "not_analyzed"
        },
        "summary": {
          "type": "string",
          "index": "analyzed"
        },
        "description": {
          "type": "string",
          "index": "analyzed"
        },
        "date_created": {
          "type": "date",
          "format": "basic_date_time_no_millis"
        }
      }
    }
  }
  """

  def foo = {
    val indexer = Indexer.transport(settings = Map("cluster.name" -> "elasticsearch"), host = "127.0.0.1")
    
    // indexer.deleteIndex(ticketIndex)
    if(!indexer.exists(ticketIndex)) {
      indexer.createIndex(ticketIndex, settings = Map("number_of_shards" -> "1"))
      indexer.waitTillActive()
      indexer.putMapping(ticketIndex, ticketType, ticketMapping)
    }
    // indexer.refresh()
  }
  
  def indexTicket(ticket: FullTicket) {

    val indexer = Indexer.transport(settings = Map("cluster.name" -> "elasticsearch"), host = "127.0.0.1")

    val tdoc: Map[String,String] = Map(
      "project_id" -> ticket.projectId.toString,
      "project_name" -> ticket.projectName,
      "priority_id" -> ticket.priorityId.toString,
      "priority_name" -> ticket.priorityName,
      // "resolution_id" -> ticket.resolutionId.toString,
      "resolution_name" -> ticket.resolutionName.getOrElse(""),
      // "proposed_resolution_id" -> ticket.proposedResolutionId.toString,
      // "proposed_resolution_name" -> ticket.proposedResolutionName.getOrElse(""),
      "reporter_id" -> ticket.reporterId.toString,
      "reporter_name" -> ticket.reporterName,
      "severity_id" -> ticket.severityId.toString,
      "severity_name" -> ticket.severityName,
      "status_id" -> ticket.statusId.toString,
      "status_name" -> ticket.statusName,
      "type_id" -> ticket.typeId.toString,
      "type_name" -> ticket.typeName,
      "summary" -> ticket.summary,
      "description" -> ticket.description.getOrElse("")
    )

    indexer.index(ticketIndex, ticketType, ticket.id.get.toString, toJson(tdoc).toString)
  }
  
  def searchTicket(page: Int, count: Int, query: String) : Page[SearchHit] = {
    
    val indexer = Indexer.transport(settings = Map("cluster.name" -> "elasticsearch"), host = "127.0.0.1")
    val response = indexer.search(
      query = queryString(query),
      facets = Seq(termsFacet("Type").field("type_name")),
      fields = List("summary")
    )
    
    Page(response.hits.hits, page, count, response.hits.totalHits)
  }
}