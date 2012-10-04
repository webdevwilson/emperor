package controllers

import emp._
import collection.JavaConversions._
import models.SearchModel
import org.elasticsearch.search.facet.terms.strings._
import play.api.mvc._

object Search extends Controller with Secured {

  def index(page: Int, count: Int, query: String) = IsAuthenticated() { implicit request =>

    val filters = request.queryString filterKeys { key =>
      key match {
        case "assignee"   => true
        case "project"    => true
        case "priority"   => true
        case "reporter"   => true
        case "resolution" => true
        case "severity"   => true
        case "status"     => true
        case "type"       => true
        case _            => false // Nothing else is useful as a filter
      }
    }

    val userId = request.session.get("user_id").get.toLong
    val q = models.SearchQuery(
      userId = userId, page = page, count = count, query = query,
      filters = filters
    )
    val result = SearchModel.searchTicket(q)

    Ok(views.html.search.index(filters, result)(request))
  }
}
