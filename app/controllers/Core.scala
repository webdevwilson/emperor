package controllers

import emp._
import emp.util.Search._
import play.api._
import play.api.mvc._
import play.api.mvc.Security._
import play.db._
import models.{ProjectModel,SearchModel}
import org.slf4j.{Logger,LoggerFactory}
import emp.util.Search._

object Core extends Controller with Secured {

  def index(page: Int = 1, count: Int = 10) = IsAuthenticated() { implicit request =>

    val userId = request.user.id.get
    val projects = models.ProjectModel.getAll(userId = userId)

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

    val query = SearchQuery(userId = userId, filters = filters, page = page, count = count)

    val response = SearchModel.searchEvent(query)

    Ok(views.html.index(response, projects))
  }
}
