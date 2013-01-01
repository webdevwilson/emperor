package controllers.api

import emp.JsonFormats._
import com.codahale.jerkson.Json._
import controllers._
import models.{UserModel,UserTokenModel}
import play.api._
import play.api.mvc._
import play.api.libs.Jsonp
import play.api.libs.json.Json

object User extends Controller with Secured {

  def startsWith(q: Option[String], callback: Option[String]) = IsAuthenticated() { implicit request =>

    q match {
      case Some(query) => {

        val json = Json.toJson(UserModel.getStartsWith(query))

        callback match {
          case Some(callback) => Ok(Jsonp(callback, json))
          case None => Ok(json)
        }
      }
      case None => NotFound
    }
  }

  def tokens(userId: Long, callback: Option[String]) = IsAuthenticated() { implicit request =>
    val maybeUser = UserModel.getById(userId)
    maybeUser match {
      case Some(user) => {
        val tokens = UserTokenModel.getByUser(userId)
        val json = Json.toJson(tokens.items.toSeq)
        callback match {
          case Some(callback) => Ok(Jsonp(callback, json))
          case None => Ok(json)
        }
      }
      case _ => NotFound
    }
  }
}