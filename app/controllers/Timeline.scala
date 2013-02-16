package controllers

import emp.util.Search._
import play.api._
import play.api.mvc._
import play.api.mvc.Security._
import play.api.db._
import models.SearchModel
import emp.util.Search._

object Timeline extends Controller with Secured {

  def index(page: Int, count: Int, query: String) = IsAuthenticated() { implicit request =>

    val filters = request.queryString filterKeys { key =>
      key match {
        case "project"=> true
        case "user"   => true
        case _        => false // Nothing else is useful as a filter
      }
    }

    val userId = request.user.id.get

    val q = SearchQuery(userId = userId, page = page, count = count, query = query, filters = filters)

    val result = SearchModel.searchEvent(q)

    Ok(views.html.timeline.index(result, filters)(request))
  }
}
