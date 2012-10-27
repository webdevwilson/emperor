package controllers

import emp._
import collection.JavaConversions._
import models.SearchModel
import org.elasticsearch.search.facet.terms.strings._
import play.api.mvc._

object Search extends Controller with Secured {

  def index(page: Int, count: Int, query: String, sort: Option[String] = None, order: Option[String] = None) = IsAuthenticated() { implicit request =>

    // XXX This can go if we use the responses filter's in the templates
    val filters = request.queryString filterKeys { key =>
      SearchModel.ticketFilterMap.get(key).isDefined
    }

    val userId = request.user.id.get
    val sort = request.queryString.get("sort").map({ vals => Some(vals.head) }).getOrElse(None);
    val order = request.queryString.get("order").map({ vals => Some(vals.head) }).getOrElse(None);
    val q = emp.util.Search.SearchQuery(
      userId = userId, page = page, count = count, query = query,
      filters = filters, sortBy = sort, sortOrder = order
    )
    val result = SearchModel.searchTicket(q)

    Ok(views.html.search.index(filters, result)(request))
  }
}
