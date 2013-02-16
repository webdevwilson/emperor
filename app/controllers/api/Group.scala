package controllers.api

import emp.JsonFormats._
import controllers._
import models.{GroupModel,UserModel}
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
  def addUser(id: Long, username: String, callback: Option[String]) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val maybeUser = UserModel.getByUsername(username)

    maybeUser match {
      case Some(user) => {
        val gu = GroupModel.addUser(user.id.get, id)
        val json = Json.toJson(gu.get) // This might be none XXX
        callback.map({ cb =>
          Ok(Jsonp(cb, json))
        }).getOrElse(Ok(json))
      }
      case None => NotFound
    }
  }

  /**
   * Remove a user from the specified group.
   */
  def removeUser(id: Long, userId: Long, callback: Option[String]) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    GroupModel.removeUser(userId, id)
    val json = Json.toJson(Map("ok" -> "ok"))
    callback match {
      case Some(callback) => Ok(Jsonp(callback, json))
      case None => Ok(json)
    }
  }

  /**
   * Get the users in a group
   */
  def users(id: Long, callback: Option[String]) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>
    val users = GroupModel.getGroupUsersForGroup(id)
    val json = Json.toJson(users)
    callback match {
      case Some(callback) => Ok(Jsonp(callback, json))
      case None => Ok(json)
    }
  }
}