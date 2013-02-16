package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import controllers._
import emp.JsonFormats._
import models.GroupModel

object Group extends Controller with Secured {

  def startsWith() = IsAuthenticated() { implicit request =>

    val param = request.queryString.get("query");

    param match {
      case Some(query) => Ok(Json.toJson(GroupModel.getStartsWith(query.head).map({ g => Json.toJson(g) })))
      case None => NotFound
    }
  }
}