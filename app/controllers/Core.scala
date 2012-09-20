package controllers

import emp._
import play.api._
import play.api.mvc._
import play.api.mvc.Security._
import play.db._
import models.{ProjectModel,SearchModel}
import org.slf4j.{Logger,LoggerFactory}

object Core extends Controller with Secured {

  val logger = LoggerFactory.getLogger("application")

  def index = IsAuthenticated() { implicit request =>

    val projects = models.ProjectModel.getAll

    val filters = request.queryString filterKeys { key =>
      key match {
        case "project"    => true
        case "priority"   => true
        case "resolution" => true
        case "severity"   => true
        case "type"       => true
        case _            => false // Nothing else is useful as a filter
      }
    }

    val response = SearchModel.searchEvent(1, 10, "", filters) // XX fixed page, count, query

    Ok(views.html.index(response, projects))
  }
}
