package models

import com.traackr.scalastic.elasticsearch.Indexer
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.facet.FacetBuilders._
import org.elasticsearch.index.query.{BaseQueryBuilder,FilterBuilder}
import org.elasticsearch.index.query.FilterBuilders._
import org.elasticsearch.index.query.QueryBuilders._
import play.api.libs.json.Json._
import play.api.libs.json._

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

  def indexComment(comment: Comment) {

    val indexer = Indexer.transport(settings = Map("cluster.name" -> "elasticsearch"), host = "127.0.0.1")

    val cdoc: Map[String,JsValue] = Map(
      "ticket_id" -> JsNumber(comment.ticketId),
      "user_id" -> JsNumber(comment.userId),
      "user_realname" -> JsString(comment.realName),
      "content" -> JsString(comment.content)
      // XXX date_created
    )

    indexer.index(ticketCommentIndex, ticketCommentType, comment.id.get.toString, toJson(cdoc).toString)
  }
  
  def indexTicket(ticket: FullTicket) {

    val indexer = Indexer.transport(settings = Map("cluster.name" -> "elasticsearch"), host = "127.0.0.1")

    val tdoc: Map[String,JsValue] = Map(
      "project_id" -> JsNumber(ticket.projectId),
      "project_name" -> JsString(ticket.projectName),
      "priority_id" -> JsNumber(ticket.priorityId),
      "priority_name" -> JsString(ticket.priorityName),
      // "resolution_id" -> ticket.resolutionId.toString,
      "resolution_name" -> JsString(ticket.resolutionName.getOrElse("")),
      // "proposed_resolution_id" -> ticket.proposedResolutionId.toString,
      // "proposed_resolution_name" -> ticket.proposedResolutionName.getOrElse(""),
      "reporter_id" -> JsNumber(ticket.reporterId),
      "reporter_name" -> JsString(ticket.reporterName),
      "severity_id" -> JsNumber(ticket.severityId),
      "severity_name" -> JsString(ticket.severityName),
      "status_id" -> JsNumber(ticket.statusId),
      "status_name" -> JsString(ticket.statusName),
      "type_id" -> JsNumber(ticket.typeId),
      "type_name" -> JsString(ticket.typeName),
      "summary" -> JsString(ticket.summary),
      "description" -> JsString(ticket.description.getOrElse(""))
      // XXX date_created
    )

    indexer.index(ticketIndex, ticketType, ticket.id.get.toString, toJson(tdoc).toString)
  }
  
  def indexHistory(changeId: Long, ticket: FullTicket, old: FullTicket) {
    
    val indexer = Indexer.transport(settings = Map("cluster.name" -> "elasticsearch"), host = "127.0.0.1")
    
    val projChanged = ticket.projectId match {
      case old.projectId => false
      case _ => true
    }
    val prioChanged = ticket.priorityId match {
      case old.priorityId => false
      case _ => true
    }
    val resoChanged = ticket.resolutionId match {
      case Some(res) if old.resolutionId.isEmpty => true // We have one now, true!
      case Some(res) if !old.resolutionId.isEmpty => res != old.resolutionId.get // True if changed
      case None if old.resolutionId.isEmpty => false // nothing and nothing, false
      case _ => true // true otherwise!
    }
    val repChanged = ticket.reporterId match {
      case x if x == old.reporterId => false
      case _ => true
    }
    val sevChanged = ticket.severityId match {
      case x if x == old.severityId => false
      case _ => true
    }
    val statChanged = ticket.statusId match {
      case old.statusId => false
      case _ => true
    }
    val typeChanged = ticket.typeId match {
      case old.statusId => false
      case _ => true
    }
    val summChanged = ticket.summary match {
      case old.summary => false
      case _ => true
    }
    val descChanged = ticket.description match {
      case Some(res) if old.description.isEmpty => true // We have one now, true!
      case Some(res) if !old.description.isEmpty => res != old.description.get // True if changed
      case None if old.description.isEmpty => false // nothing and nothing, false
      case _ => true // true otherwise!
    }

    val hdoc: Map[String,JsValue] = Map(
      "ticket_id" -> JsNumber(ticket.id.get),
      "project_id" -> JsNumber(ticket.projectId),
      "project_name" -> JsString(ticket.projectName),
      "project_changed" -> JsBoolean(projChanged),
      "priority_id" -> JsNumber(ticket.priorityId),
      "priority_name" -> JsString(ticket.priorityName),
      "priorityChanged" -> JsBoolean(prioChanged),
      // "resolution_id" -> ticket.resolutionId.toString,
      "resolution_name" -> JsString(ticket.resolutionName.getOrElse("")),
      "resolution_changed" -> JsBoolean(resoChanged),
      // "proposed_resolution_id" -> ticket.proposedResolutionId.toString,
      // "proposed_resolution_name" -> ticket.proposedResolutionName.getOrElse(""),
      "reporter_id" -> JsNumber(ticket.reporterId),
      "reporter_name" -> JsString(ticket.reporterName),
      "reporter_changed" -> JsBoolean(repChanged),
      "severity_id" -> JsNumber(ticket.severityId),
      "severity_name" -> JsString(ticket.severityName),
      "severity_changed" -> JsBoolean(sevChanged),
      "status_id" -> JsNumber(ticket.statusId),
      "status_name" -> JsString(ticket.statusName),
      "status_changed" -> JsBoolean(statChanged),
      "type_id" -> JsNumber(ticket.typeId),
      "type_name" -> JsString(ticket.typeName),
      "type_changed" -> JsBoolean(typeChanged),
      "summary" -> JsString(ticket.summary),
      "summary_changed" -> JsBoolean(summChanged),
      "description" -> JsString(ticket.description.getOrElse("")),
      "description_changed" -> JsBoolean(descChanged)
      // XXX date_occurred
    )
    println(toJson(hdoc).toString)
    indexer.index(ticketHistoryIndex, ticketHistoryType, changeId.toString, toJson(hdoc).toString)
  }

  def searchChange(page: Int, count: Int, query: String, filters: Map[String, Seq[String]]) : SearchResponse = {
    
    // This shouldn't have to live here. It annoys me. Surely there's a better
    // way.
    var q = query
    if(q.isEmpty) {
      q = "*"
    }
    
    var actualQuery : BaseQueryBuilder = queryString(q)
    
    // If we have filters, build up a filterquery and swap out our actualQuery
    // with a filtered version!
    // if(!filters.isEmpty) {
    //   val fqs : Iterable[FilterBuilder] = filters map {
    //     case (key, values) => termFilter(key + "_name", values.head).asInstanceOf[FilterBuilder]
    //   }
    //   actualQuery = filteredQuery(actualQuery, andFilter(fqs.toSeq:_*))
    // }
    
    // XXX use page and count
    val indexer = Indexer.transport(settings = Map("cluster.name" -> "elasticsearch"), host = "127.0.0.1")
    indexer.search(
      query = actualQuery,
      indices = Seq("ticket_histories"),
      facets = Seq(
        // termsFacet("user_id").field("user_id"), // XXX readd this
        termsFacet("changed_priority").field("priority_changed"),
        termsFacet("changed_reporter").field("reporter_changed"),
        termsFacet("changed_resolution").field("resolution_changed"),
        termsFacet("changed_severity").field("severity_changed"),
        termsFacet("changed_status").field("status_changed")
      ),
      fields = List("*"),
      size = Some(count),
      from = page match {
        case 0 => Some(0)
        case 1 => Some(0)
        case _ => Some((page * count) - 1)
      }
    ) // XXX order
  }
  
  def searchComment(page: Int, count: Int, query: String, filters: Map[String, Seq[String]]) : SearchResponse = {
    
    // This shouldn't have to live here. It annoys me. Surely there's a better
    // way.
    var q = query
    if(q.isEmpty) {
      q = "*"
    }
    
    var actualQuery : BaseQueryBuilder = queryString(q)
    
    // If we have filters, build up a filterquery and swap out our actualQuery
    // with a filtered version!
    // if(!filters.isEmpty) {
    //   val fqs : Iterable[FilterBuilder] = filters map {
    //     case (key, values) => termFilter(key + "_name", values.head).asInstanceOf[FilterBuilder]
    //   }
    //   actualQuery = filteredQuery(actualQuery, andFilter(fqs.toSeq:_*))
    // }
    
    // XXX use page and count
    val indexer = Indexer.transport(settings = Map("cluster.name" -> "elasticsearch"), host = "127.0.0.1")
    indexer.search(
      query = actualQuery,
      indices = Seq("ticket_comments"),
      facets = Seq(
        termsFacet("user_id").field("user_id")
      ),
      fields = List("content", "user_realname"),
      size = Some(count),
      from = page match {
        case 0 => Some(0)
        case 1 => Some(0)
        case _ => Some((page * count) - 1)
      }
    ) // order!!
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
      indices = Seq("tickets"),
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
    ) // order!!
  }
}