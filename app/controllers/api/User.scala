package controllers.api

import emp.JsonFormats._
import com.codahale.jerkson.Json._
import controllers._
import models.UserModel
import play.api._
import play.api.mvc._
import play.api.libs.Jsonp
import play.api.libs.json.Json

object User extends Controller with Secured {

  def startsWith(q: Option[String]) = IsAuthenticated() { implicit request =>

    q match {
      case Some(query) => {

        val json = Json.toJson(UserModel.getStartsWith(query))

        request.queryString.get("callback").flatMap(_.headOption) match {
          case Some(callback) => Ok(Jsonp(callback, json))
          case None => Ok(json)
        }
      }
      case None => NotFound
    }
  }
}