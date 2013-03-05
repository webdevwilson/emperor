package controllers.api

import emp.JsonFormats._
import controllers._
import models.{GroupModel,UserModel}
import play.api._
import play.api.mvc._
import play.api.libs.Jsonp
import play.api.libs.json.Json

object Group extends Controller with Secured {

  /**
   * Retrieve a group by id.
   */
  def item(id: Long, callback: Option[String]) = IsAuthenticated() { implicit request =>

    GroupModel.getById(id).map({ project =>

      val json = Json.toJson(project)
      callback.map({ callback =>
        Ok(Jsonp(callback, json))
      }).getOrElse(Ok(json))
    }).getOrElse(NotFound(Json.toJson(Map("error" -> "api.unknown.entity"))))
  }

  /**
   * List all groups.
   */
  def index(callback: Option[String]) = IsAuthenticated() { implicit request =>
    val json = Json.toJson(GroupModel.getAll)

    callback.map({ callback =>
      Ok(Jsonp(callback, json))
    }).getOrElse(Ok(json))
  }

  /**
   * Find groups that start with a string
   */
  def startsWith(q: Option[String], callback: Option[String]) = IsAuthenticated() { implicit request =>

    q.map({ query =>
      val json = Json.toJson(GroupModel.getStartsWith(query))

      callback.map({ callback =>
        Ok(Jsonp(callback, json))
      }).getOrElse(Ok(json))
    }).getOrElse(NotFound(Json.toJson(Map("error" -> "api.unknown.entity"))))
  }

  /**
   * Add a user to the specified group.
   */
  def addUser(id: Long, username: String, callback: Option[String]) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    // XXX Check if group exists!
    UserModel.getByUsername(username).map({ user =>

      val gu = GroupModel.addUser(user.id.get, id)
      val json = Json.toJson(gu.get) // This might be none XXX
      callback.map({ cb =>
        Ok(Jsonp(cb, json))
      }).getOrElse(Ok(json))
    }).getOrElse(NotFound(Json.toJson(Map("error" -> "api.unknown.entity"))))
  }

  /**
   * Remove a user from the specified group.
   */
  def removeUser(id: Long, userId: Long, callback: Option[String]) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    GroupModel.removeUser(userId, id)
    val json = Json.toJson(Map("ok" -> "ok"))
    callback.map({ callback =>
      Ok(Jsonp(callback, json))
    }).getOrElse(Ok(json))
  }

  /**
   * Get the users in a group
   */
  def users(id: Long, callback: Option[String]) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>
    val users = GroupModel.getGroupUsersForGroup(id)
    val json = Json.toJson(users)
    callback.map({ callback =>
      Ok(Jsonp(callback, json))
    }).getOrElse(Ok(json))
  }
}