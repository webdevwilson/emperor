package controllers.api

import chc.JsonFormats._
import controllers._
import models.TicketModel
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
}