package controllers.api

import emp.JsonFormats._
import controllers._
import models._
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.libs.json.Json
import play.api.libs.Jsonp
import play.api.mvc._

object Project extends Controller with Secured {

  def item(id: Long, callback: Option[String]) = IsAuthenticated(projectId = Some(id), perm = "PERM_PROJECT_BROWSE") { implicit request =>

    ProjectModel.getById(id) match {
      case Some(project) => {

        val json = Json.toJson(project)

        callback match {
          case Some(callback) => Ok(Jsonp(callback, json))
          case None => Ok(json)
        }

      }
      case None => NotFound
    }
  }
}