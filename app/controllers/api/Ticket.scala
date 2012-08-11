package controllers.api

import chc.JsonFormats._
import controllers._
import models._
import play.api.libs.json._
import play.api.libs.json.Json
import play.api.mvc._

object Ticket extends Controller with Secured {

  def item(ticketId: String) = IsAuthenticated { implicit request =>

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
}