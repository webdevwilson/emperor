package models

import com.traackr.scalastic.elasticsearch.Indexer
import java.text.SimpleDateFormat
import java.util.{Date,TimeZone}
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.{BaseQueryBuilder,FilterBuilder}
import org.elasticsearch.index.query.FilterBuilders._
import org.elasticsearch.index.query.QueryBuilders._
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.facet.FacetBuilders._
import org.elasticsearch.search.sort._
import play.api.libs.json.Json._
import play.api.libs.json._

object SearchModel {
  
  val dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")
  dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"))

  // Embedded ES
  // XXX turn off network for this?
  // XXX set data directory!
  val indexer = Indexer.local.start
  
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
        "assignee_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "assignee_name": {
          "type": "string",
          "index": "not_analyzed"
        },
        "attention_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "attention_name": {
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
      "user_realname" {
        "type": "string",
        "index": "not_analyzed"
      },
      "project_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "old_project_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "project_name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "old_project_name": {
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
      "old_priority_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "priority_name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "old_priority_name": {
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
      "old_resolution_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "resolution_name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "old_resolution_name": {
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
      "old_proposed_resolution_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "proposed_resolution_name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "old_proposed_resolution_name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "proposed_resolution_changed": {
        "type": "boolean",
        "index": "not_analyzed"
      },
      "assignee_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "old_assignee_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "assignee_name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "old_assignee_name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "assignee_changed": {
        "type": "boolean",
        "index": "not_analyzed"
      },
      "attention_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "old_attention_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "attention_name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "old_attention_name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "attention_changed": {
        "type": "boolean",
        "index": "not_analyzed"
      },
      "reporter_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "old_reporter_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "reporter_name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "old_reporter_name": {
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
      "old_severity_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "severity_name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "old_severity_name": {
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
      "old_status_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "status_name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "old_status_name": {
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
      "old_type_id": {
        "type": "long",
        "index": "not_analyzed"
      },
      "type_name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "old_type_name": {
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
      "old_summary": {
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
      "old_description": {
        "type": "string",
        "index": "analyzed"
      },
      "description_changed": {
        "type": "boolean",
        "index": "not_analyzed"
      },
      "date_occurred": {
        "type": "date",
        "format": "basic_date_time_no_millis"
      }
    }
  }
  }
  """

  def foo = {
    
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

    val cdoc: Map[String,JsValue] = Map(
      "ticket_id"     -> JsNumber(comment.ticketId),
      "user_id"       -> JsNumber(comment.userId),
      "user_realname" -> JsString(comment.realName),
      "content"       -> JsString(comment.content),
      "date_created"  -> JsString(dateFormatter.format(comment.dateCreated))
    )
    indexer.index(ticketCommentIndex, ticketCommentType, comment.id.get.toString, toJson(cdoc).toString)
  }
  
  def indexTicket(ticket: FullTicket) {

    val resId = ticket.resolution.id match {
      case Some(id)   => JsNumber(id)
      case None       => JsNull
    }
    val resName = ticket.resolution.name match {
      case Some(name) => JsString(name)
      case None       => JsString("TICK_RESO_UNRESOLVED")
    }

    val tdoc: Map[String,JsValue] = Map(
      "project_id"      -> JsNumber(ticket.project.id),
      "project_name"    -> JsString(ticket.project.name),
      "priority_id"     -> JsNumber(ticket.priority.id),
      "priority_name"   -> JsString(ticket.priority.name),
      "resolution_id"   -> resId,
      "resolution_name" -> resName,
      // "proposed_resolution_id" -> ticket.proposedResolutionId.toString,
      // "proposed_resolution_name" -> ticket.proposedResolutionName.getOrElse(""),
      "reporter_id"     -> JsNumber(ticket.reporter.id),
      "reporter_name"   -> JsString(ticket.reporter.name),
      "assignee_id"     -> { ticket.assigneeId match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      } },
      // XXX assignee name
      "attention_id"     -> { ticket.attentionId match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      } },
      // XXX attention name
      "severity_id"     -> JsNumber(ticket.severity.id),
      "severity_name"   -> JsString(ticket.severity.name),
      "status_id"       -> JsNumber(ticket.status.id),
      "status_name"     -> JsString(ticket.status.name),
      "type_id"         -> JsNumber(ticket.ttype.id),
      "type_name"       -> JsString(ticket.ttype.name),
      "summary"         -> JsString(ticket.summary),
      "description"     -> JsString(ticket.description.getOrElse("")),
      "date_created"    -> JsString(dateFormatter.format(ticket.dateCreated))
    )

    indexer.index(ticketIndex, ticketType, ticket.id.get.toString, toJson(tdoc).toString)
  }
  
  def indexHistory(history: TicketFullHistory) {
    
    val ticket = history.newTicket
    val old = history.oldTicket

    val projChanged = ticket.project.id match {
      case old.project.id => false
      case _ => true
    }
    val prioChanged = ticket.priority.id match {
      case old.priority.id => false
      case _ => true
    }
    val resoChanged = ticket.resolution.id match {
      case Some(res) if old.resolution.id.isEmpty => true // We have one now, true!
      case Some(res) if !old.resolution.id.isEmpty => res != old.resolution.id.get // True if changed
      case None if old.resolution.id.isEmpty => false // nothing and nothing, false
      case _ => true // true otherwise!
    }
    val assChanged = ticket.assigneeId match {
      case Some(ass) if old.assigneeId.isEmpty => true // We have one now, true!
      case Some(ass) if !old.assigneeId.isEmpty => ass != old.assigneeId.get // True if changed
      case None if old.assigneeId.isEmpty => false // nothing and nothing, false
      case _ => true // true otherwise!
    }
    val attChanged = ticket.attentionId match {
      case Some(att) if old.attentionId.isEmpty => true // We have one now, true!
      case Some(att) if !old.attentionId.isEmpty => att != old.attentionId.get // True if changed
      case None if old.attentionId.isEmpty => false // nothing and nothing, false
      case _ => true // true otherwise!
    }
    val repChanged = ticket.reporter.id match {
      case x if x == old.reporter.id => false
      case _ => true
    }
    val sevChanged = ticket.severity.id match {
      case x if x == old.severity.id => false
      case _ => true
    }
    val statChanged = ticket.status.id match {
      case old.status.id => false
      case _ => true
    }
    val typeChanged = ticket.ttype.id match {
      case old.ttype.id => false
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
      "ticket_id"         -> JsNumber(ticket.id.get),
      "user_id"           -> JsNumber(history.userId),
      // "user_realname"     -> JsString(userRealName), // XXX
      "project_id"        -> JsNumber(ticket.project.id),
      "old_project_id"    -> JsNumber(old.project.id),
      "project_name"      -> JsString(ticket.project.name),
      "old_project_name"  -> JsString(old.project.name),
      "project_changed"   -> JsBoolean(projChanged),
      "priority_id"       -> JsNumber(ticket.priority.id),
      "old_priority_id"   -> JsNumber(old.priority.id),
      "priority_name"     -> JsString(ticket.priority.name),
      "old_priority_name" -> JsString(old.priority.name),
      "priorityChanged"   -> JsBoolean(prioChanged),
      "resolution_id"     -> { ticket.resolution.id match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      } },
      // "old_resolution_id" -> JsNumber(old.resolutionId.getOrElse("")), XXX
      "resolution_name"   -> { ticket.resolution.name match {
        case Some(name) => JsString(name)
        case None       => JsString("TICK_RESO_UNRESOLVED")
      } },
      "old_resolution_name" -> JsString(old.resolution.name.getOrElse("")),
      "resolution_changed"-> JsBoolean(resoChanged),
      // "proposed_resolution_id" -> ticket.proposedResolutionId.toString, XXX
      // "proposed_resolution_name" -> ticket.proposedResolutionName.getOrElse(""), XXX
      "assignee_id"       -> { ticket.assigneeId match {
        case Some(assId)=> JsNumber(assId)
        case None       => JsNull
      } },
      // "assignee_name"     -> JsString(ticket.assigneeName),
      "assignee_changed"  -> JsBoolean(assChanged),
      "attention_id"      -> { ticket.attentionId match {
        case Some(attId) => JsNumber(attId)
        case None        => JsNull
      } },
      // "attention_name"    -> JsString(ticket.assigneeName),
      "attention_changed" -> JsBoolean(attChanged),
      "reporter_id"       -> JsNumber(ticket.reporter.id),
      "old_reporter_id"   -> JsNumber(old.reporter.id),
      "reporter_name"     -> JsString(ticket.reporter.name),
      "old_reporter_name" -> JsString(old.reporter.name),
      "reporter_changed"  -> JsBoolean(repChanged),
      "severity_id"       -> JsNumber(ticket.severity.id),
      "old_severity_id"   -> JsNumber(old.severity.id),
      "severity_name"     -> JsString(ticket.severity.name),
      "old_severity_name" -> JsString(old.severity.name),
      "severity_changed"  -> JsBoolean(sevChanged),
      "status_id"         -> JsNumber(ticket.status.id),
      "old_status_id"     -> JsNumber(old.status.id),
      "status_name"       -> JsString(ticket.status.name),
      "old_status_name"   -> JsString(old.status.name),
      "status_changed"    -> JsBoolean(statChanged),
      "type_id"           -> JsNumber(ticket.ttype.id),
      "old_type_id"       -> JsNumber(old.ttype.id),
      "type_name"         -> JsString(ticket.ttype.name),
      "old_type_name"     -> JsString(old.ttype.name),
      "type_changed"      -> JsBoolean(typeChanged),
      "summary"           -> JsString(ticket.summary),
      "old_summary"       -> JsString(old.summary),
      "summary_changed"   -> JsBoolean(summChanged),
      "description"       -> JsString(ticket.description.getOrElse("")),
      "old_description"   -> JsString(old.description.getOrElse("")),
      "description_changed" -> JsBoolean(descChanged),
      "date_occurred"     -> JsString(dateFormatter.format(new Date()))
    )
    indexer.index(ticketHistoryIndex, ticketHistoryType, history.id.toString, toJson(hdoc).toString)
  }

  def reIndex {
    // Nix all the existing documents
    indexer.deleteByQuery(
      indices = Seq(ticketIndex, ticketHistoryIndex, ticketCommentIndex),
      types = Seq(ticketType, ticketHistoryType, ticketCommentType)
    )
    // Reindex all tickets
    TicketModel.getAllFull.foreach { ticket =>
      indexTicket(ticket)
    }
    // Reindex all ticket comments
    TicketModel.getAllComments.foreach { comment =>
      indexComment(comment)
    }

    // XXX Need history!
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
    if(!filters.isEmpty) {
      val fqs : Iterable[FilterBuilder] = filters map {
        case (key, values) => termFilter(key, values.head).asInstanceOf[FilterBuilder]
      }
      actualQuery = filteredQuery(actualQuery, andFilter(fqs.toSeq:_*))
    }
    
    indexer.search(
      query = actualQuery,
      indices = Seq("ticket_histories"),
      facets = Seq(
        // termsFacet("user_id").field("user_id"), // XXX Broken due to differing classes
        termsFacet("changed_priority").field("priority_changed"),
        termsFacet("changed_reporter").field("reporter_changed"),
        termsFacet("changed_resolution").field("resolution_changed"),
        termsFacet("changed_severity").field("severity_changed"),
        termsFacet("changed_status").field("status_changed")
      ),
      size = Some(count),
      from = page match {
        case 0 => Some(0)
        case 1 => Some(0)
        case _ => Some((page - 1)  * count)
      },
      sorting = Seq("date_occurred" -> SortOrder.DESC)
    )
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
    if(!filters.isEmpty) {
      val fqs : Iterable[FilterBuilder] = filters map {
        case (key, values) => termFilter(key, values.head).asInstanceOf[FilterBuilder]
      }
      actualQuery = filteredQuery(actualQuery, andFilter(fqs.toSeq:_*))
    }
    
    indexer.search(
      query = actualQuery,
      indices = Seq("ticket_comments"),
      facets = Seq(
        termsFacet("user_id").field("user_id")
      ),
      fields = List("content", "user_id", "user_realname", "date_created"),
      size = Some(count),
      from = page match {
        case 0 => Some(0)
        case 1 => Some(0)
        case _ => Some((page * count) - 1)
      },
      sorting = Seq("date_created" -> SortOrder.DESC)
    )
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
    
    indexer.search(
      query = actualQuery,
      indices = Seq("tickets"),
      facets = Seq(
        termsFacet("resolution").field("resolution_name"),
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
      },
      sorting = Seq("date_created" -> SortOrder.DESC)
    )
  }
  
  def shutdown {
    indexer.stop
  }
}