package models

import com.traackr.scalastic.elasticsearch.Indexer
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.facet.FacetBuilders._
import org.elasticsearch.index.query.{BaseQueryBuilder,FilterBuilder}
import org.elasticsearch.index.query.FilterBuilders._
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
        "type_name": {
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

  val ticketCommentIndex = "ticket_comments"
  val ticketCommentType = "ticket_comment"
  val ticketCommentMapping = """
  {
    "ticket": {
      "properties": {
        "ticket_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "user_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "user_realname": {
          "type": "string",
          "index": "not_analyzed"
        },
        "content": {
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

  val ticketHistoryIndex = "ticket_histories"
  val ticketHistoryType = "ticket_history"
  val ticketHistoryMapping = """
  {
  "ticket_history": {
    "properties": {
      "ticket_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "user_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "project_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "project_name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "project_changed": {
        "type": "boolean",
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
      "priority_changed": {
        "type": "boolean",
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
      "resolution_changed": {
        "type": "boolean",
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
      "proposed_resolution_changed": {
        "type": "boolean",
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
      "reporter_changed": {
        "type": "boolean",
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
      "severity_changed": {
        "type": "boolean",
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
      "severity_changed": {
        "type": "boolean",
        "index": "not_analyzed"
      },
      "type_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "type_name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "type_changed": {
        "type": "boolean",
        "index": "not_analyzed"
      },
      "summary": {
        "type": "string",
        "index": "analyzed"
      },
      "summary_changed": {
        "type": "boolean",
        "index": "not_analyzed"
      },
      "description": {
        "type": "string",
        "index": "analyzed"
      },
      "description_changed": {
        "type": "boolean",
        "index": "not_analyzed"
      },
      "date_occured": {
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
    if(!indexer.exists(ticketCommentIndex)) {
      indexer.createIndex(ticketCommentIndex, settings = Map("number_of_shards" -> "1"))
      indexer.waitTillActive()
      indexer.putMapping(ticketCommentIndex, ticketCommentType, ticketCommentMapping)
    }
    if(!indexer.exists(ticketHistoryIndex)) {
      indexer.createIndex(ticketHistoryIndex, settings = Map("number_of_shards" -> "1"))
      indexer.waitTillActive()
      indexer.putMapping(ticketHistoryIndex, ticketHistoryType, ticketHistoryMapping)
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
  
  def searchTicket(page: Int, count: Int, query: String, filters: Map[String, Seq[String]]) : SearchResponse = {
    
    // This shouldn't have to live here. It annoys me. Surely there's a better
    // way.
    var q = query
    if(q.isEmpty) {
      q = "*"
    }
    
    var actualQuery : BaseQueryBuilder = queryString(q)
    
    // If we have filters, build up a filterquery and swap out our actualQuery
    // with a filtered version!
    if(!filters.isEmpty) {
      val fqs : Iterable[FilterBuilder] = filters map {
        case (key, values) => termFilter(key + "_name", values.head).asInstanceOf[FilterBuilder]
      }
      actualQuery = filteredQuery(actualQuery, andFilter(fqs.toSeq:_*))
    }
    
    // XXX use page and count
    val indexer = Indexer.transport(settings = Map("cluster.name" -> "elasticsearch"), host = "127.0.0.1")
    indexer.search(
      query = actualQuery,
      facets = Seq(
        termsFacet("type").field("type_name"),
        termsFacet("project").field("project_name"),
        termsFacet("priority").field("priority_name"),
        termsFacet("severity").field("severity_name"),
        termsFacet("status").field("status_name")
      ),
      fields = List("summary"),
      size = Some(count),
      from = page match {
        case 0 => Some(0)
        case 1 => Some(0)
        case _ => Some((page * count) - 1)
      }
    )
  }
}