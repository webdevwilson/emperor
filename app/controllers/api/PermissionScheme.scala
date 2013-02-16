package controller.api

import emp.JsonFormats._
import controllers._
import models.PermissionSchemeModel
import play.api._
import play.api.mvc._
import play.api.libs.Jsonp
import play.api.libs.json.Json
import play.api.libs.json._

object PermissionScheme extends Controller with Secured {

  def permissionGroups(id: Long, permissionId: String, callback: Option[String]) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val groups = PermissionSchemeModel.getGroupsForPermission(id, permissionId)

    val json = Json.toJson(groups)

    callback match {
      case Some(callback) => Ok(Jsonp(callback, json))
      case None => Ok(json)
    }
  }

  def permissionUsers(id: Long, permissionId: String, callback: Option[String]) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val users = PermissionSchemeModel.getUsersForPermission(id, permissionId)

    val json = Json.toJson(users)

    callback match {
      case Some(callback) => Ok(Jsonp(callback, json))
      case None => Ok(json)
    }
  }

  /**
   * Revoke permission from the specified group.
   */
  def removeGroup(id: Long, permission: String, groupId: Long, callback: Option[String]) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>
    PermissionSchemeModel.removeGroupFromScheme(permissionSchemeId = id, perm = permission, groupId = groupId);

    val json = Json.toJson(Map("ok" -> "ok"))

    callback match {
      case Some(callback) => Ok(Jsonp(callback, json))
      case None => Ok(json)
    }
  }

  /**
   * Revoke permission from the specified group.
   */
  def removeUser(id: Long, permission: String, userId: Long, callback: Option[String]) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>
    PermissionSchemeModel.removeUserFromScheme(permissionSchemeId = id, perm = permission, userId = userId);

    val json = Json.toJson(Map("ok" -> "ok"))

    callback match {
      case Some(callback) => Ok(Jsonp(callback, json))
      case None => Ok(json)
    }
  }
}