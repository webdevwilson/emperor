package controllers.api

import chc.JsonFormats._
import controllers._
import models._
import play.api.libs.json.Json
import play.api.mvc._

object Ticket extends Controller with Secured {

  def item(ticketId: String) = IsAuthenticated { implicit request =>

    val ticket = TicketModel.getFullById(ticketId)

    ticket match {
      case Some(t) => Ok(Json.toJson(t))
      case None => NotFound
    }
  }

  def workflow(ticketId: String) = IsAuthenticated { implicit request =>

    val ticket = TicketModel.getFullById(ticketId)

    ticket match {
      case Some(t) => {

        val prevStatus = WorkflowModel.getPreviousStatus(t.workflowStatusId)
        val nextStatus = WorkflowModel.getNextStatus(t.workflowStatusId)

        val statuses: Map[String,Option[WorkflowStatus]] = Map(
          "previous" -> prevStatus,
          "next" -> nextStatus
        )

        Ok(Json.toJson(statuses))
      }
      case None => NotFound
    }
  }
}