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
    TicketModel.removeLink(id)
    // XXX This should verify that the ticket is one of the parent or child!!
    val json = Json.toJson(Map("ok" -> "ok"))
    callback match {
      case Some(callback) => Ok(Jsonp(callback, json))
      case None => Ok(json)
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

    def startsWith(q: Option[String], callback: Option[String]) = IsAuthenticated() { implicit request =>

    q match {
      case Some(query) => {

        val sq = emp.util.Search.SearchQuery(
          userId = request.user.id.get, page = 1, count = 10, query = "ticket_id: " + query + "*"
        )
        val res = SearchModel.searchTicket(sq)

        val tickets = res.pager.items.map({ ticket => ticket.ticketId }).toSeq;

        callback match {
          case Some(callback) => Ok(Jsonp(callback, Json.toJson(tickets)))
          case None => Ok(Json.toJson(tickets))
        }
      }
      case None => NotFound
    }
  }
}