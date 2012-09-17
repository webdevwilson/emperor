package controllers.api

import chc.JsonFormats._
import controllers._
import models._
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.libs.json.Json
import play.api.mvc._

object Project extends Controller with Secured {

  def item(id: Long) = IsAuthenticated(projectId = Some(id), perm = "PERM_PROJECT_BROWSE") { implicit request =>

    ProjectModel.getById(id) match {
      case Some(project) => {

        Ok(Json.toJson(project))
      }
      case None => NotFound
    }
  }
}