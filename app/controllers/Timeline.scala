package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Security._
import play.db._
import models.SearchModel
import org.slf4j.{Logger,LoggerFactory}

object Timeline extends Controller with Secured {

  val logger = LoggerFactory.getLogger("application")

  def index(page: Int, count: Int, query: String) = IsAuthenticated() { implicit request =>

    val filters = request.queryString filterKeys { key =>
      key match {
        case "project"=> true
        case "user"   => true
        case _        => false // Nothing else is useful as a filter
      }
    }

    val result = SearchModel.searchEvent(page = page, count = count, query = query, filters = filters)

    Ok(views.html.timeline.index(result, filters)(request))
  }
}
