package controllers.api

import emp.JsonFormats._
import com.codahale.jerkson.Json._
import controllers._
import models.PermissionSchemeModel
import play.api._
import play.api.mvc._
import play.api.libs.Jsonp
import play.api.libs.json.Json

object Permission extends Controller with Secured {

  /**
   * List Permissions
   */
  def index(callback: Option[String]) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val json = Json.toJson(PermissionSchemeModel.getAllPermissions)

    callback match {
      case Some(callback) => Ok(Jsonp(callback, json))
      case None => Ok(json)
    }
  }
}