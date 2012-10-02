package controller.api

import emp.JsonFormats._
import com.codahale.jerkson.Json._
import controllers._
import models.PermissionSchemeModel
import play.api._
import play.api.mvc._
import play.api.libs.Jsonp
import play.api.libs.json.Json

object PermissionScheme extends Controller with Secured {

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