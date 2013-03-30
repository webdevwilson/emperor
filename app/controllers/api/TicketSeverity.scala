package controllers.api

import emp.JsonFormats._
import controllers._
import models._
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.libs.json.Json
import play.api.libs.Jsonp
import play.api.mvc._

object TicketSeverity extends Controller with Secured {

  def index(callback: Option[String]) = IsAuthenticated() { implicit request =>
    val json = Json.toJson(TicketSeverityModel.getAll)
    callback.map({ callback =>
      Ok(Jsonp(callback, json))
    }).getOrElse(Ok(json))
  }

  def item(sevId: Long, callback: Option[String]) = IsAuthenticated() { implicit request =>

    TicketSeverityModel.getById(sevId).map({ sev =>

      val json = Json.toJson(sev)
      callback.map({ callback =>
	    	Ok(Jsonp(callback, json))
      }).getOrElse(Ok(json))
    }).getOrElse(NotFound(Json.toJson(Map("error" -> "api.unknown.entity"))))
  }
}