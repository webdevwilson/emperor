package controllers.api

import chc.JsonFormats._
import controllers._
import models._
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.libs.json.Json
import play.api.mvc._

object LinkTicket extends Controller with Secured {

  def item(id: String) = IsAuthenticated() { implicit request =>

    val tid = request.session.get("link_ticket")

    tid match {
      case Some(ticketId) => {
        val ticket = TicketModel.getFullById(ticketId)

        ticket match {

          case Some(t) => {
            val lt: Map[String,JsValue] = Map(
              "disabled"  -> JsBoolean(id match {
                case x if x == ticketId  => true
                case _    => false
              }),
              "ticket_id" -> JsString(t.ticketId),
              "summary"   -> JsString(t.summary),
              "short_summary"   -> JsString(t.summary match {
                case x if x.length > 15 => x.take(20) + "&hellip;"
                case x => x
              })
            )

            Ok(Json.toJson(lt))
          }
          case None => NotFound
        }
      }
      case None => Ok("")
    }
  }

  def link(id: String) = IsAuthenticated() { implicit request =>

    request.body.asJson.map { json =>
      (json \ "ticket_id").asOpt[String].map { ticketId =>

        val ticket = TicketModel.getFullById(ticketId)

        ticket match {

          case Some(t) => {
            val lt: Map[String,JsValue] = Map(
              "disabled"  -> JsBoolean(id match {
                case x if x == ticketId  => true
                case _    => false
              }),
              "ticket_id" -> JsString(t.ticketId),
              "summary"   -> JsString(t.summary),
              "short_summary"   -> JsString(t.summary match {
                case x if x.length > 15 => x.take(20) + "&hellip;"
                case x => x
              })
            )

            Ok(Json.toJson(lt)).withSession(session + ("link_ticket" -> ticketId))
          }
          case None => NotFound
        }
      }.getOrElse {
        // This is stupid, but it worked with frontend stuff…
        val resp = Map(
          "ok" -> Messages("ticket.linker.stop", id)
        )

        Ok(Json.toJson(resp)).withSession(session - "link_ticket")
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }
}