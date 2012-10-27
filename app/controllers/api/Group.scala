package controllers.api

import emp.JsonFormats._
import com.codahale.jerkson.Json._
import controllers._
import models.GroupModel
import play.api._
import play.api.mvc._
import play.api.libs.Jsonp
import play.api.libs.json.Json

object Group extends Controller with Secured {

  def startsWith(q: Option[String], callback: Option[String]) = IsAuthenticated() { implicit request =>

    q match {
      case Some(query) => {

        val json = Json.toJson(GroupModel.getStartsWith(query))

        callback match {
          case Some(callback) => Ok(Jsonp(callback, json))
          case None => Ok(json)
        }
      }
      case None => NotFound
    }
  }

  /**
   * Add a user to the specified group.
   */
  def addUser(id: Long, userId: Long, callback: Option[String]) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>
    println("WHAT THE FUCK")
    GroupModel.addUser(userId, id) // XXX This should return something…
    val json = Json.toJson(Map("ok" -> "ok"))
    callback match {
      case Some(callback) => Ok(Jsonp(callback, json))
      case None => Ok(json)
    }
  }

  /**
   * Remove a user from the specified group.
   */
  def removeUser(id: Long, userId: Long, callback: Option[String]) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>
    GroupModel.removeUser(userId, id);
    val json = Json.toJson(Map("ok" -> "ok"))
    callback match {
      case Some(callback) => Ok(Jsonp(callback, json))
      case None => Ok(json)
    }
  }
}