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
  // XXX mapping blows up
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
          "type": "string",
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
        "type": "string",
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
      "date_created": {
        "type": "date",
        "format": "basic_date_time_no_millis"
      }
    }
  }
  }
  """

  def checkIndices = {
    
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
    indexer.refresh()
  }

  def indexComment(comment: Comment) {

    val cdoc: Map[String,JsValue] = Map(
      "ticket_id"     -> JsString(comment.ticketId),
      "user_id"       -> JsNumber(comment.userId),
      "user_realname" -> JsString(comment.realName),
      "content"       -> JsString(comment.content),
      "date_created"  -> JsString(dateFormatter.format(comment.dateCreated))
    )
    indexer.index(ticketCommentIndex, ticketCommentType, comment.id.get.toString, toJson(cdoc).toString)
    indexer.refresh()
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

    indexer.index(ticketIndex, ticketType, ticket.ticketId, toJson(tdoc).toString)
    indexer.refresh()
  }
  
  def indexHistory(oldTick: FullTicket, newTick: FullTicket) {
    
    val projChanged = newTick.project.id match {
      case oldTick.project.id => false
      case _ => true
    }
    val prioChanged = newTick.priority.id match {
      case oldTick.priority.id => false
      case _ => true
    }
    val resoChanged = newTick.resolution.id match {
      case Some(res) if oldTick.resolution.id.isEmpty => true // We have one now, true!
      case Some(res) if !oldTick.resolution.id.isEmpty => res != oldTick.resolution.id.get // True if changed
      case None if oldTick.resolution.id.isEmpty => false // nothing and nothing, false
      case _ => true // true otherwise!
    }
    val assChanged = newTick.assigneeId match {
      case Some(ass) if oldTick.assigneeId.isEmpty => true // We have one now, true!
      case Some(ass) if !oldTick.assigneeId.isEmpty => ass != oldTick.assigneeId.get // True if changed
      case None if oldTick.assigneeId.isEmpty => false // nothing and nothing, false
      case _ => true // true otherwise!
    }
    val attChanged = newTick.attentionId match {
      case Some(att) if oldTick.attentionId.isEmpty => true // We have one now, true!
      case Some(att) if !oldTick.attentionId.isEmpty => att != oldTick.attentionId.get // True if changed
      case None if oldTick.attentionId.isEmpty => false // nothing and nothing, false
      case _ => true // true otherwise!
    }
    val repChanged = newTick.reporter.id match {
      case x if x == oldTick.reporter.id => false
      case _ => true
    }
    val sevChanged = newTick.severity.id match {
      case x if x == oldTick.severity.id => false
      case _ => true
    }
    val statChanged = newTick.status.id match {
      case oldTick.status.id => false
      case _ => true
    }
    val typeChanged = newTick.ttype.id match {
      case oldTick.ttype.id => false
      case _ => true
    }
    val summChanged = newTick.summary match {
      case oldTick.summary => false
      case _ => true
    }
    val descChanged = newTick.description match {
      case Some(res) if oldTick.description.isEmpty => true // We have one now, true!
      case Some(res) if !oldTick.description.isEmpty => res != oldTick.description.get // True if changed
      case None if oldTick.description.isEmpty => false // nothing and nothing, false
      case _ => true // true otherwise!
    }

    val hdoc: Map[String,JsValue] = Map(
      "ticket_id"         -> JsString(newTick.ticketId),
      "user_id"           -> JsNumber(newTick.userId),
      // "user_realname"     -> JsString(userRealName), // XXX
      "project_id"        -> JsNumber(newTick.project.id),
      "old_project_id"    -> JsNumber(oldTick.project.id),
      "project_name"      -> JsString(newTick.project.name),
      "old_project_name"  -> JsString(oldTick.project.name),
      "project_changed"   -> JsBoolean(projChanged),
      "priority_id"       -> JsNumber(newTick.priority.id),
      "old_priority_id"   -> JsNumber(oldTick.priority.id),
      "priority_name"     -> JsString(newTick.priority.name),
      "old_priority_name" -> JsString(oldTick.priority.name),
      "priorityChanged"   -> JsBoolean(prioChanged),
      "resolution_id"     -> { newTick.resolution.id match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      } },
      // "old_resolution_id" -> JsNumber(oldTick.resolutionId.getOrElse("")), XXX
      "resolution_name"   -> { newTick.resolution.name match {
        case Some(name) => JsString(name)
        case None       => JsString("TICK_RESO_UNRESOLVED")
      } },
      "old_resolution_name" -> JsString(oldTick.resolution.name.getOrElse("")),
      "resolution_changed"-> JsBoolean(resoChanged),
      // "proposed_resolution_id" -> newTick.proposedResolutionId.toString, XXX
      // "proposed_resolution_name" -> newTick.proposedResolutionName.getOrElse(""), XXX
      "assignee_id"       -> { newTick.assigneeId match {
        case Some(assId)=> JsNumber(assId)
        case None       => JsNull
      } },
      // "assignee_name"     -> JsString(newTick.assigneeName),
      "assignee_changed"  -> JsBoolean(assChanged),
      "attention_id"      -> { newTick.attentionId match {
        case Some(attId) => JsNumber(attId)
        case None        => JsNull
      } },
      // "attention_name"    -> JsString(newTick.assigneeName),
      "attention_changed" -> JsBoolean(attChanged),
      "reporter_id"       -> JsNumber(newTick.reporter.id),
      "old_reporter_id"   -> JsNumber(oldTick.reporter.id),
      "reporter_name"     -> JsString(newTick.reporter.name),
      "old_reporter_name" -> JsString(oldTick.reporter.name),
      "reporter_changed"  -> JsBoolean(repChanged),
      "severity_id"       -> JsNumber(newTick.severity.id),
      "old_severity_id"   -> JsNumber(oldTick.severity.id),
      "severity_name"     -> JsString(newTick.severity.name),
      "old_severity_name" -> JsString(oldTick.severity.name),
      "severity_changed"  -> JsBoolean(sevChanged),
      "status_id"         -> JsNumber(newTick.status.id),
      "old_status_id"     -> JsNumber(oldTick.status.id),
      "status_name"       -> JsString(newTick.status.name),
      "old_status_name"   -> JsString(oldTick.status.name),
      "status_changed"    -> JsBoolean(statChanged),
      "type_id"           -> JsNumber(newTick.ttype.id),
      "old_type_id"       -> JsNumber(oldTick.ttype.id),
      "type_name"         -> JsString(newTick.ttype.name),
      "old_type_name"     -> JsString(oldTick.ttype.name),
      "type_changed"      -> JsBoolean(typeChanged),
      "summary"           -> JsString(newTick.summary),
      "old_summary"       -> JsString(oldTick.summary),
      "summary_changed"   -> JsBoolean(summChanged),
      "description"       -> JsString(newTick.description.getOrElse("")),
      "old_description"   -> JsString(oldTick.description.getOrElse("")),
      "description_changed" -> JsBoolean(descChanged),
      "date_created"      -> JsString(dateFormatter.format(new Date()))
    )
    indexer.index(ticketHistoryIndex, ticketHistoryType, newTick.id.toString, toJson(hdoc).toString)
    indexer.refresh()
  }

  def reIndex {

    indexer.deleteIndex(ticketIndex)
    indexer.deleteIndex(ticketHistoryIndex)
    indexer.deleteIndex(ticketCommentIndex)
    checkIndices

    // Nix all the existing documents
    // indexer.deleteByQuery(
    //   indices = Seq(ticketIndex, ticketHistoryIndex, ticketCommentIndex),
    //   types = Seq(ticketType, ticketHistoryType, ticketCommentType)
    // )
    // Reindex all tickets
    TicketModel.getAllCurrentFull.foreach { ticket =>
      indexTicket(ticket)
      val count = TicketModel.getAllFullCountById(ticket.ticketId)
      if(count > 1) {
        TicketModel.getAllFullById(ticket.ticketId).foldLeft(None: Option[FullTicket])((oldTick, newTick) => {
          oldTick.map { ot => indexHistory(oldTick = ot, newTick = newTick) }
          Some(newTick)
        })
      }
    }
    // Reindex all ticket comments
    TicketModel.getAllComments.foreach { comment =>
      indexComment(comment)
    }
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
      val fqs: Iterable[FilterBuilder] = filters map {
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
      sorting = Seq("date_created" -> SortOrder.DESC)
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
        // XXX project should be here but it's a long
      ),
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