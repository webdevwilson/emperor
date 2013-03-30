package controllers.api

import emp.JsonFormats._
import controllers._
import models._
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.libs.json.Json
import play.api.libs.Jsonp
import play.api.mvc._

object TicketType extends Controller with Secured {

  def index(callback: Option[String]) = IsAuthenticated() { implicit request =>
    val json = Json.toJson(TicketTypeModel.getAll)
    callback.map({ callback =>
      Ok(Jsonp(callback, json))
    }).getOrElse(Ok(json))
  }

  def item(typeId: Long, callback: Option[String]) = IsAuthenticated() { implicit request =>

    TicketTypeModel.getById(typeId).map({ ttype =>

      val json = Json.toJson(ttype)
      callback.map({ callback =>
	    Ok(Jsonp(callback, json))
      }).getOrElse(Ok(json))
    }).getOrElse(NotFound(Json.toJson(Map("error" -> "api.unknown.entity"))))
  }
}