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
  def removeGroup(id: Long, permission: String, groupId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>
    PermissionSchemeModel.removeGroupFromScheme(permissionSchemeId = id, perm = permission, groupId = groupId);
    Ok(Json.toJson(Map("ok" -> "ok")))
  }
}