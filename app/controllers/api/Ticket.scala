package controllers.api

import chc.JsonFormats._
import controllers._
import models._
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.libs.json.Json
import play.api.mvc._

object Ticket extends Controller with Secured {

  def index = IsAuthenticated { implicit request =>
    Ok("yay")
  }

  def item(ticketId: String) = IsAuthenticated { implicit request =>

    val lid = request.session.get("link_ticket")
    val ticket = TicketModel.getFullById(ticketId)

    ticket match {
      case Some(t) => {

        val prevStatus = WorkflowModel.getPreviousStatus(t.workflowStatusId)
        val nextStatus = WorkflowModel.getNextStatus(t.workflowStatusId)

        val statuses: Map[String,Option[WorkflowStatus]] = Map(
          "previous" -> prevStatus,
          "next" -> nextStatus
        )

        val apiTick: Map[String,JsValue] = Map(
          "ticket" -> Json.toJson(t),
          "workflow" -> Json.toJson(statuses)
        )
        Ok(Json.toJson(apiTick))
      }
      case None => NotFound
    }
  }

  def link(ticketId: String) = IsAuthenticated { implicit request =>

    request.body.asJson.map { json =>
      val childId = (json \ "child_ticket_id").asOpt[String]
      val typeId = (json \ "link_type_id").asOpt[Long]

      val maybeLink: Either[String,Option[Link]] = if(childId.isDefined && typeId.isDefined) {
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
          case Some(l) => Ok(Json.toJson(l))
          case None => InternalServerError("Error occurred creating link.")
        }
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def links(ticketId: String) = IsAuthenticated { implicit request =>

    val links = TicketModel.getLinks(ticketId)

    // <li>@Messages(link.typeName + "_INVERT") <a href="@routes.Ticket.item(link.childId)">@link.childId</a></li>

    Ok(Json.toJson(links))
  }
}