package models

import emp._
import emp.JsonFormats._
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
import play.api._
import play.api.Play.current
import play.api.libs.json.Json._
import play.api.libs.json._

import org.elasticsearch.client._, transport._
import org.elasticsearch.common.settings.ImmutableSettings._
import org.elasticsearch.node._, NodeBuilder._
import scala.collection.JavaConversions._

object SearchModel {

  val dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")

  val config = Play.configuration.getConfig("emperor")
  // Embedded ES
  val settings = Map(
    "path.data" -> config.get.getString("directory").getOrElse("")
  )
  val indexer = Indexer.at(
    nodeBuilder.local(true).data(true).settings(
      settingsBuilder.put(settings)
    ).node
  ).start

  // Ticket ES index
  val ticketIndex = "tickets"
  val ticketType = "ticket"
  val ticketFilterMap = Map(
    "assignee"    -> "assignee_name",
    "project"     -> "project_name",
    "priority"    -> "priority_name",
    "reporter"    -> "reporter_name",
    "resolution"  -> "resolution_name",
    "severity"    -> "severity_name",
    "status"      -> "status_name",
    "type"        -> "type_name"
  )
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
        "priority_color": {
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
        "severity_color": {
          "type": "string",
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
        "type_color": {
          "type": "string",
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

  // Event ES index
  val eventIndex = "events"
  val eventType = "event"
  val eventFilterMap = Map(
    "user" -> "user_realname",
    "project" -> "project_name"
  )
  val eventMapping = """
  {
    "event": {
      "properties": {
        "project_id": {
          "type": "long",
          "index": "not_analyzed"
        },
        "project_name": {
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
        "ekey": {
          "type": "string",
          "index": "not_analyzed"
        },
        "etype": {
          "type": "string",
          "index": "not_analyzed"
        },
        "content": {
          "type": "string",
          "index": "not_analyzed"
        },
        "url": {
          "type": "string",
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

  // Ticket Comment ES index
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

  // Ticket History ES index
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

  /**
   * Check that all the necessary indices exist.  If they don't, create them.
   */
  def checkIndices = {

    if(!indexer.exists(eventIndex)) {
      indexer.createIndex(eventIndex, settings = Map("number_of_shards" -> "1"))
      indexer.waitTillActive()
      indexer.putMapping(eventIndex, eventType, eventMapping)
    }
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

  /**
   * Index an event.
   */
  def indexEvent(event: Event) {

    indexer.index(eventIndex, eventType, null, toJson(event).toString)
    indexer.refresh()
  }

  /**
   * Index a comment.
   */
  def indexComment(comment: Comment) {

    indexer.index(ticketCommentIndex, ticketCommentType, comment.id.get.toString, toJson(comment).toString)

    val ft = TicketModel.getFullById(comment.ticketId).get

    SearchModel.indexEvent(Event(
      projectId     = ft.project.id,
      projectName   = ft.project.name,
      userId        = ft.user.id,
      userRealName  = ft.user.name,
      eKey          = comment.ticketId,
      eType         = "comment",
      content       = comment.content,
      url           = "",
      dateCreated   = ft.dateCreated
    ))

    indexer.refresh()
  }

  /**
   * Index a ticket.
   */
  def indexTicket(ticket: FullTicket) {

    indexer.index(ticketIndex, ticketType, ticket.ticketId, toJson(ticket).toString)

    indexer.refresh()
  }

  /**
   * Index a history item by comparing a new and old ticket.
   */
  def indexHistory(oldTick: FullTicket, newTick: FullTicket) {

    val projChanged = newTick.project.id != oldTick.project.id
    val prioChanged = newTick.priority.id != oldTick.priority.id
    val resoChanged = newTick.resolution.id != oldTick.resolution.id
    val propResoChanged = newTick.proposedResolution.id != oldTick.proposedResolution.id
    val assChanged = newTick.assignee.id != oldTick.assignee.id
    val attChanged = newTick.attention.id != oldTick.attention.id
    val repChanged = newTick.reporter.id != oldTick.reporter.id
    val sevChanged = newTick.severity.id != oldTick.severity.id
    val statChanged = newTick.status.id != oldTick.status.id
    val typeChanged = newTick.ttype.id != oldTick.ttype.id
    val summChanged = newTick.summary != oldTick.summary
    val descChanged = newTick.description != oldTick.description

    val hdoc: Map[String,JsValue] = Map(
      "ticket_id"         -> JsString(newTick.ticketId),
      "user_id"           -> JsNumber(newTick.user.id),
      "user_realname"     -> JsString(newTick.user.name),
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
      "old_resolution_id" -> { oldTick.resolution.id match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      } },
      "resolution_name"   -> { newTick.resolution.name match {
        case Some(name) => JsString(name)
        case None       => JsString("TICK_RESO_UNRESOLVED")
      } },
      "old_resolution_name" -> JsString(oldTick.resolution.name.getOrElse("")),
      "resolution_changed"-> JsBoolean(resoChanged),
      "proposed_resolution_id" -> { newTick.proposedResolution.id match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      } },
      "old_proposed_resolution_id" -> { oldTick.proposedResolution.id match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      } },
      "proposed_resolution_name" -> { newTick.proposedResolution.name match {
        case Some(name) => JsString(name)
        case None       => JsNull
      } },
      "old_proposed_resolution_name" -> { oldTick.proposedResolution.name match {
        case Some(name) => JsString(name)
        case None       => JsNull
      } },
      "proposed_resolution_changed"-> JsBoolean(propResoChanged),
      "assignee_id"       -> { newTick.assignee.id match {
        case Some(assId)=> JsNumber(assId)
        case None       => JsNull
      } },
      "old_assignee_id"       -> { oldTick.assignee.id match {
        case Some(assId)=> JsNumber(assId)
        case None       => JsNull
      } },
      "assignee_name"     -> { newTick.assignee.name match {
        case Some(name) => JsString(name)
        case None       => JsNull
      } },
      "old_assignee_name"     -> { oldTick.assignee.name match {
        case Some(name) => JsString(name)
        case None       => JsNull
      } },
      "assignee_changed"  -> JsBoolean(assChanged),
      "attention_id"      -> { newTick.attention.id match {
        case Some(attId) => JsNumber(attId)
        case None        => JsNull
      } },
      "old_attention_id"      -> { oldTick.attention.id match {
        case Some(attId) => JsNumber(attId)
        case None        => JsNull
      } },
      "attention_name"    -> { newTick.attention.name match {
        case Some(name) => JsString(name)
        case None       => JsNull
      } },
      "old_attention_name"    -> { oldTick.attention.name match {
        case Some(name) => JsString(name)
        case None       => JsNull
      } },
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
      "date_created"      -> JsString(dateFormatter.format(newTick.dateCreated))
    )
    indexer.index(ticketHistoryIndex, ticketHistoryType, newTick.id.toString, toJson(hdoc).toString)

    // XXX Do something special for resolution changes, they are ticked_resolved
    // and status changes.
    indexEvent(Event(
      projectId     = newTick.project.id,
      projectName   = newTick.project.name,
      userId        = newTick.user.id,
      userRealName  = newTick.user.name,
      eKey          = newTick.ticketId,
      eType         = "ticket_change",
      content       = newTick.summary,
      url           = "",
      dateCreated   = newTick.dateCreated
    ))

    indexer.refresh()
  }

  /**
   * Delete all the existing indexes and recreate them. Then iterate over
   * all the tickets and index each one and it's history.  Finally
   * reindex all the ticket comments.
   */
  def reIndex {

    indexer.deleteIndex(eventIndex)
    indexer.deleteIndex(ticketIndex)
    indexer.deleteIndex(ticketHistoryIndex)
    indexer.deleteIndex(ticketCommentIndex)
    checkIndices

    // Reindex all tickets and their history
    TicketModel.getAllCurrentFull.foreach { ticket =>
      indexTicket(ticket)
      val count = TicketModel.getAllFullCountById(ticket.ticketId)
      if(count > 1) {
        TicketModel.getAllFullById(ticket.ticketId).foldLeft(None: Option[FullTicket])((oldTick, newTick) => {
          // First run will NOT index history because oldTick is None (as None starts the fold)
          oldTick.map { ot => indexHistory(oldTick = ot, newTick = newTick) }

          Some(newTick)
        })
      }
    }
    // Reindex all ticket comments
    // XXX This should be nested within the above loop to avoid having to
    // re-fetch every fullticket.
    TicketModel.getAllComments.foreach { comment =>
      indexComment(comment)
    }
  }

  // XXX
  def searchProjectStats(projectId: Long): SearchResponse = {

    var actualQuery = filteredQuery(queryString("*"), andFilter(termFilter("project_id", projectId)))

    indexer.search(
      query = actualQuery,
      indices = Seq("tickets"),
      facets = Seq(
      )
    )
  }

  /**
   * Search for ticket changes, or history.
   */
  def searchChange(page: Int, count: Int, query: String, filters: Map[String, Seq[String]]): SearchResponse = {

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

  /**
   * Search for ticket comments.
   */
  def searchComment(page: Int, count: Int, query: String, filters: Map[String, Seq[String]], sorting: Seq[Tuple2[String,SortOrder]] = Seq("date_created" -> SortOrder.DESC)): SearchResponse = {

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
      indices = Seq(ticketCommentIndex),
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
      sorting = sorting
    )
  }

  /**
   * Search for events.
   */
  def searchEvent(page: Int, count: Int, query: String, filters: Map[String, Seq[String]]): SearchResult[org.elasticsearch.search.SearchHit] = {

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
        case (key, values) => termFilter(eventFilterMap.get(key).getOrElse(key), values.head).asInstanceOf[FilterBuilder]
      }
      actualQuery = filteredQuery(actualQuery, andFilter(fqs.toSeq:_*))
    }

    val res = indexer.search(
      query = actualQuery,
      indices = Seq(eventIndex),
      facets = Seq(
        termsFacet("project").field("project_name"),
        termsFacet("user").field("user_realname")
      ),
      fields = List("project_id", "project_name", "user_id", "user_realname", "ekey", "etype", "content", "url", "date_created"),
      size = Some(count),
      from = page match {
        case 0 => Some(0)
        case 1 => Some(0)
        case _ => Some((page * count) - 1)
      },
      sorting = Seq("date_created" -> SortOrder.DESC)
    )

    val pager = Page(res.hits.hits, page, count, res.hits.totalHits)
    Library.parseSearchResponse(pager = pager, response = res)
  }

  /**
   * Search for a ticket.
   */
  def searchTicket(page: Int, count: Int, query: String, filters: Map[String, Seq[String]]): SearchResult[org.elasticsearch.search.SearchHit] = {

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
        case (key, values) => termFilter(ticketFilterMap.get(key).getOrElse(key), values.head).asInstanceOf[FilterBuilder]
      }
      actualQuery = filteredQuery(actualQuery, andFilter(fqs.toSeq:_*))
    }

    val res = indexer.search(
      query = actualQuery,
      indices = Seq(ticketIndex),
      facets = Seq(
        termsFacet("resolution").field("resolution_name"),
        termsFacet("type").field("type_name"),
        termsFacet("project").field("project_name"),
        termsFacet("priority").field("priority_name"),
        termsFacet("severity").field("severity_name"),
        termsFacet("status").field("status_name"),
        termsFacet("assignee").field("assignee_name"),
        termsFacet("reporter").field("reporter_name")
      ),
      size = Some(count),
      from = page match {
        case 0 => Some(0)
        case 1 => Some(0)
        case _ => Some((page * count) - 1)
      },
      sorting = Seq("date_created" -> SortOrder.DESC)
    )

    val pager = Page(res.hits.hits, page, count, res.hits.totalHits)
    Library.parseSearchResponse(pager = pager, response = res)
  }

  /**
   * Shutdown ElasticSearch.
   */
  def shutdown {
    indexer.stop
  }
}
