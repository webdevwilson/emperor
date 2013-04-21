package models

import emp.text.Renderer
import emp.util.Pagination.Page
import emp.util.Search
import emp.util.Search._
import emp.JsonFormats._
import java.net.URL
import scalastic.elasticsearch._, SearchParameterTypes._
import play.api._
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.Json._
import play.api.libs.json._

import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query._
import org.elasticsearch.index.query.FilterBuilders._
import org.elasticsearch.index.query.QueryBuilders._
import org.elasticsearch.search.facet.terms.strings._
import org.elasticsearch.search.facet.terms.longs.InternalLongTermsFacet
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.facet.FacetBuilders._
import org.elasticsearch.search.sort._

import org.elasticsearch.client._, transport._
import org.elasticsearch.common.settings.ImmutableSettings._
import org.elasticsearch.node._, NodeBuilder._

import org.joda.time.format.DateTimeFormat

// XXX
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import wabisabi.Client

import emp._
import scala.collection.JavaConversions._

object SearchModel {

  val dateFormatter = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss'Z'").withZoneUTC()

  val config = Play.configuration.getConfig("emperor")

  // Start ES, even though we may not need it. XXX
  val indexer = Indexer.at(
    nodeBuilder.local(true).data(true).settings(
      settingsBuilder.put(Map(
        "path.data" -> config.get.getString("es.directory").getOrElse("data")
      ))
    ).node
  ).start

  val esURL = config.get.getString("es.url").getOrElse("http://localhost:9200")
  // Use a transport-based ES client when we're supplied with a URL

  // val url = new URL(esURL)
  Logger.debug("Trying to connect to " + esURL)
  val esClient = new Client(esURL)

  // Ticket ES index
  val ticketIndex = "tickets"
  val ticketType = "ticket"
  val ticketFilterMap = Map(
    "assignee"    -> "assignee_name",
    "project"     -> "project_name",
    "priority"    -> "priority_name",
    "resolution"  -> "resolution_name",
    "severity"    -> "severity_name",
    "status"      -> "status_name",
    "type"        -> "type_name"
  )
  val ticketSortMap = Map(
    "date_created"-> "date_created",
    "id"          -> "id",
    "priority"    -> "priority_position",
    "severity"    -> "severity_position",
    "type"        -> "type_name",
    "resolution"  -> "resolution_id"
  )
  val ticketMapping = """
  {
    "ticket": {
      "properties": {
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
        "priority_position": {
          "type": "integer",
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
        "severity_position": {
          "type": "integer",
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
        "workflow_status_id": {
          "type": "long",
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
    "user_id" -> "user_id",
    "user" -> "user_realname",
    "project" -> "project_name"
  )
  val eventSortMap = Map(
    "date_created" -> "date_created"
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
        "user_email": {
          "type": "string",
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
  val ticketCommentFilterMap = Map(
    "ticket_id" -> "ticket_id",
    "type"      -> "type"
  )
  val ticketCommentSortMap = Map(
    "date_created"-> "date_created"
  )
  val ticketCommentMapping = """
  {
    "ticket": {
      "properties": {
        "type": {
          "type": "string",
          "index": "not_analyzed"
        },
        "project_id": {
          "type": "long",
          "index": "not_analyzed"
        },
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

  // Facets
  val ticketCommentFacets = Map(
    "user_id" -> "user_id"
  )
  val eventFacets = Map(
    "project" -> "project_name",
    "user" -> "user_realname"
  )
  val ticketFacets = Map(
    "resolution" -> "resolution_name",
    "type" -> "type_name",
    "project" -> "project_name",
    "priority" -> "priority_name",
    "severity" -> "severity_name",
    "status" -> "status_name",
    "assignee" -> "assignee_name"
  )

  val indexSettings = "{\"settings\": { \"index\": { \"number_of_shards\": 1 } } }"

  /**
   * Check that all the necessary indices exist.  If they don't, create them.
   */
  def checkIndices = {

    val indexNames = List(eventIndex, ticketIndex, ticketCommentIndex, ticketHistoryIndex)
    val typeNames = List(eventType, ticketType, ticketCommentType, ticketHistoryType)
    val mappings = List(eventMapping, ticketMapping, ticketCommentMapping, ticketHistoryMapping)

    // Create the indices and set the mappings.
    indexNames.zipWithIndex.foreach({ case (name, i) =>
      Logger.debug(s"Creating index $name")
      esClient.verifyIndex(name = name) onFailure {
        // If the verify fails, that means it doesn't exist. We don't care about
        // success because that means the index is there.
        case _ => {
          esClient.createIndex(name = name, settings = Some(indexSettings)) map { f =>
            esClient.health(indices = Seq(name), waitForNodes = Some("1")) // XXX Number of shards should be configurable
          } map { f =>
            esClient.putMapping(indices = Seq(name), `type` = typeNames(i), body = mappings(i))
          } recover {
            case x: Throwable => {
              Logger.error(s"Failed to create index: $name")
              // Rethrow!
              throw x
            }
          }
        }
      }
    })
    // Block for the last index, we want to make sure everything is done.
    Await.result(esClient.health(indices = Seq(indexNames.last), waitForNodes = Some("1")), Duration(5, "seconds")) // XXX Number of shards should be configurable
  }

  /**
   * Index an event.
   *
   * @param event The event to index
   * @param block Boolean determining if this method should block and refresh the index before returning.
   */
  def indexEvent(event: Event, block: Boolean = false) {

    indexer.index(index = eventIndex, `type` = eventType, id = null, source = toJson(event).toString, refresh = Some(block))
  }

  /**
   * Index a comment.
   *
   * @param comment The comment to index
   * @param block Boolean determining if this method should block and refresh the index before returning
   */
  def indexComment(comment: Comment, block: Boolean = false) {

    indexer.index(index = ticketCommentIndex, `type` = ticketCommentType, id = comment.id.get.toString, source = toJson(comment).toString, refresh = Some(block))

    val ft = TicketModel.getFullById(comment.ticketId).get

    val user = UserModel.getById(comment.userId).get

    SearchModel.indexEvent(Event(
      projectId     = ft.project.id,
      projectName   = ft.project.name,
      userId        = user.id.get,
      userRealName  = user.realName,
      eKey          = comment.ticketId.toString,
      eType         = comment.ctype,
      content       = Renderer.render(Some(comment.content)),
      url           = controllers.routes.Ticket.item("commits", ft.ticketId).url + "#comment-" + comment.id.get,
      dateCreated   = comment.dateCreated
    ))
  }

  /**
   * Index a ticket.

   * @param ticket The ticket to index
   * @param block Boolean determining if this method should block and refresh the index before returning
   */
  def indexTicket(ticket: FullTicket, block: Boolean = false) {

    indexer.index(index = ticketIndex, `type` = ticketType, id = ticket.ticketId, source = toJson(ticket).toString, refresh = Some(block))
  }

  /**
   * Index a history item by comparing a new and old ticket.
   *
   * @param oldTick The old ticket, before changes
   * @param newTick The new ticket, after changes
   * @param block Boolean determining if this method should block and refresh the index before returning
   */
  def indexHistory(oldTick: FullTicket, newTick: FullTicket, block: Boolean = false) {

    val projChanged = newTick.project.id != oldTick.project.id
    val prioChanged = newTick.priority.id != oldTick.priority.id
    val resoChanged = newTick.resolution.id != oldTick.resolution.id
    val assChanged = newTick.assignee.id != oldTick.assignee.id
    val attChanged = newTick.attention.id != oldTick.attention.id
    val sevChanged = newTick.severity.id != oldTick.severity.id
    val statChanged = newTick.status.id != oldTick.status.id
    val typeChanged = newTick.ttype.id != oldTick.ttype.id
    val summChanged = newTick.summary != oldTick.summary
    val descChanged = newTick.description != oldTick.description

    val hdoc: Map[String,JsValue] = Map(
      "ticket_id"         -> JsNumber(newTick.id.get),
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
      "date_created"      -> JsString(dateFormatter.print(newTick.dateCreated))
    )
    indexer.index(
      index = ticketHistoryIndex, `type` = ticketHistoryType,
      id = newTick.dataId.toString, source = toJson(hdoc).toString,
      refresh = Some(block)
    )

    // XXX Do something special for resolution changes, they are ticket_resolved
    // and status changes.
    indexEvent(Event(
      projectId     = newTick.project.id,
      projectName   = newTick.project.name,
      userId        = newTick.user.id,
      userRealName  = newTick.user.name,
      eKey          = newTick.ticketId,
      eType         = "ticket_change",
      content       = newTick.summary,
      url           = controllers.routes.Ticket.item("comments", newTick.ticketId).url, // This should really link to the history tab's specific entry
      dateCreated   = newTick.dateCreated
    ))
  }

  /**
   * Delete all the existing indexes and recreate them. Then iterate over
   * all the tickets and index each one and it's history.  Finally
   * reindex all the ticket comments.
   */
  def reIndex {

    indexer.deleteIndex(Seq(eventIndex, ticketIndex, ticketHistoryIndex, ticketCommentIndex))
    checkIndices

    // Reindex all tickets and their history
    TicketModel.getAllCurrentFull.foreach { ticket =>
      indexTicket(ticket)

      indexEvent(Event(
        projectId     = ticket.project.id,
        projectName   = ticket.project.name,
        userId        = ticket.user.id,
        userRealName  = ticket.user.name,
        eKey          = ticket.ticketId,
        eType         = "ticket_create",
        content       = ticket.summary,
        url           = controllers.routes.Ticket.item("comments", ticket.ticketId).url,
        dateCreated   = ticket.dateCreated
      ))

      val count = TicketModel.getAllFullCountById(ticket.id.get)
      if(count > 1) {
        TicketModel.getAllFullById(ticket.id.get).foldLeft(None: Option[FullTicket])((oldTick, newTick) => {
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
        termsFacet("changed_resolution").field("resolution_changed"),
        termsFacet("changed_severity").field("severity_changed"),
        termsFacet("changed_status").field("status_changed")
      ),
      size = Some(count),
      from = page match {
        case 0 => Some(0)
        case 1 => Some(0)
        case _ => Some((page - 1) * count)
      },
      sortings = Seq(FieldSort(field = "date_created", order = SortOrder.DESC))
    )
  }

  /**
   * Search for ticket comments.
   */
  // def searchComment(page: Int, count: Int, query: String, filters: Map[String, Seq[String]], sorting: Seq[Tuple2[String,SortOrder]] = Seq("date_created" -> SortOrder.DESC)): SearchResponse = {
  def searchComment(query: SearchQuery): SearchResult[Comment] = {

    val res = Search.runQuery(
      client = esClient, index = ticketCommentIndex, query = query,
      filterMap = ticketCommentFilterMap, sortMap = ticketCommentSortMap,
      ticketCommentFacets, filterProjects = false
    )
    // val jsonTrans = (__ \ "hits").json.pick[JsArray]
    val response = Await.result(res, Duration(1, "seconds"))

    val hits = (Json.parse(response) \ "hits" \ "hits" \\ "_source").map({ h => Json.fromJson[models.Comment](h).asOpt.get })

    val pager = Page(hits, query.page, query.count, 1) // XXX NO NO
    emp.util.Search.parseSearchResponse(pager = pager, response = response)
  }

  /**
   * Search for events.
   */
  def searchEvent(query: SearchQuery): SearchResult[Event] = {

    val res = Search.runQuery(esClient, eventIndex, query, eventFilterMap, eventSortMap, eventFacets)
    // val jsonTrans = (__ \ "hits").json.pick[JsArray]
    val response = Await.result(res, Duration(1, "seconds"))

    val hits = (Json.parse(response) \ "hits" \ "hits" \\ "_source").map({ h => Json.fromJson[models.Event](h).asOpt.get })

    val pager = Page(hits, query.page, query.count, 1) // XXX No No
    emp.util.Search.parseSearchResponse(pager = pager, response = response)
  }

  /**
   * Search for a ticket.
   */
  def searchTicket(query: SearchQuery): SearchResult[FullTicket] = {

    val res = runQuery(esClient, ticketIndex, query, ticketFilterMap, ticketSortMap, ticketFacets)
    val response = Await.result(res, Duration(1, "seconds"))

    val hits = (Json.parse(response) \ "hits" \ "hits" \\ "_source").map({ h => Json.fromJson[models.FullTicket](h).asOpt.get })


    val pager = Page(hits, query.page, query.count, 1) // XXX NO
    emp.util.Search.parseSearchResponse(pager = pager, response = response)
  }

  /**
   * Shutdown ElasticSearch.
   */
  def shutdown {
    indexer.stop
  }
}
