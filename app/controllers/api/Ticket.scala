package controllers.api

import emp.JsonFormats._
import controllers._
import models._
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.libs.json.Json
import play.api.libs.Jsonp
import play.api.mvc._

object Ticket extends Controller with Secured {

  def item(ticketId: String, callback: Option[String]) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_PROJECT_BROWSE") { implicit request =>

    val ticket = TicketModel.getFullById(ticketId)

    ticket match {
      case Some(t) => {

        // val prevStatus = WorkflowModel.getPreviousStatus(t.workflowStatusId)
        // val nextStatus = WorkflowModel.getNextStatus(t.workflowStatusId)

        // val statuses: Map[String,Option[WorkflowStatus]] = Map(
        //   "previous" -> prevStatus,
        //   "next" -> nextStatus
        // )

        // val apiTick: Map[String,JsValue] = Map(
        //   "ticket" -> Json.toJson(t),
        //   "workflow" -> Json.toJson(statuses)
        // )

        val json = Json.toJson(t)
        callback match {
          case Some(callback) => Ok(Jsonp(callback, json))
          case None => Ok(json)
        }
      }
      case None => NotFound
    }
  }

  def deleteLink(ticketId: String, id: Long, callback: Option[String]) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_LINK") { implicit request =>

    val maybeLink = TicketModel.getLinkById(id).map({ link =>
      if(link.parentId == ticketId || link.childId == ticketId) {
        link
      } else {
        None
      }
    })

    maybeLink match {
      case Some(link) => {
        TicketModel.removeLink(id)
        val json = Json.toJson(Map("ok" -> "ok"))
        callback match {
          case Some(callback) => Ok(Jsonp(callback, json))
          case None => Ok(json)
        }
      }
      case None => NotFound
    }
  }

  def link(ticketId: String, callback: Option[String]) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_LINK") { implicit request =>

    request.body.asJson.map { json =>
      val childId = (json \ "child_ticket_id").asOpt[String]
      val typeId = (json \ "link_type_id").asOpt[Long]

      val maybeLink: Either[String,Option[FullLink]] = if(childId.isDefined && typeId.isDefined) {
        if(childId.get == ticketId) {
          Left("Can't link ticket to itself.")
        } else {
          Right(TicketModel.link(
            linkTypeId = typeId.get, parentId = ticketId, childId = childId.get
          ))
        }
      } else {
        Left("Must have both child_ticket_id and link_type_id")
      }

      maybeLink match {
        case Left(message) => BadRequest(message)
        case Right(link) => link match {
          case Some(l) => {
            val json = Json.toJson(l)
            callback match {
              case Some(callback) => Ok(Jsonp(callback, json))
              case None => Ok(json)
            }
          }
          case None => InternalServerError("Error occurred creating link.")
        }
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def links(ticketId: String, callback: Option[String]) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_PROJECT_BROWSE") { implicit request =>

    val links = TicketModel.getLinks(ticketId)

    // XXX Need to put the actual tickets in here, at least the Edit ticket

    val json = Json.toJson(links)
    callback match {
      case Some(callback) => Ok(Jsonp(callback, json))
      case None => Ok(json)
    }
  }

  def search(page: Int, count: Int, query: String, sort: Option[String] = None, order: Option[String] = None, callback: Option[String]) = IsAuthenticated() { implicit request =>

    val filters = request.queryString filterKeys { key =>
      SearchModel.ticketFilterMap.get(key).isDefined
    }

    val userId = request.user.id.get
    val sort = request.queryString.get("sort").map({ vals => Some(vals.head) }).getOrElse(None);
    val order = request.queryString.get("order").map({ vals => Some(vals.head) }).getOrElse(None);
    val q = emp.util.Search.SearchQuery(
      userId = userId, page = page, count = count, query = query,
      filters = filters, sortBy = sort, sortOrder = order
    )
    val res = SearchModel.searchTicket(q)

    val tickets = res.pager.items.map({ ticket => ticket }).toSeq;

    callback match {
      case Some(callback) => Ok(Jsonp(callback, Json.toJson(tickets)))
      case None => Ok(Json.toJson(tickets))
    }
  }

  def startsWith(q: Option[String], callback: Option[String]) = IsAuthenticated() { implicit request =>

    q match {
      case Some(query) => {

        val sq = emp.util.Search.SearchQuery(
          userId = request.user.id.get, page = 1, count = 10, query = "ticket_id: " + query + "*"
        )
        val res = SearchModel.searchTicket(sq)

        val tickets = res.pager.items.map({ ticket => ticket }).toSeq;

        callback match {
          case Some(callback) => Ok(Jsonp(callback, Json.toJson(tickets)))
          case None => Ok(Json.toJson(tickets))
        }
      }
      case None => NotFound
    }
  }
}