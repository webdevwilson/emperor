package controllers

import chc._
import collection.JavaConversions._
import models.SearchModel
import org.elasticsearch.search.facet.terms.strings._
import play.api.mvc._

object Search extends Controller with Secured {

  def index(page: Int, count: Int, query: String) = IsAuthenticated { implicit request =>

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

    val response = SearchModel.searchTicket(page, count, query, filters)

    val pager = Page(response.hits.hits, page, count, response.hits.totalHits)

    // The response contains a list of facets, but they are generic'ed down
    // to Facet rather than their real fucking class.  Therefore we re-cast
    // them all in this list and filter any that do not have more than one
    // value. The > 1 is because a facet with only one term is useless to
    // display.
    val termfacets = response.facets.facets.map { facet =>
      facet match {
        case t: InternalStringTermsFacet => t
      }
    } filter { f => f.entries.size > 1 }

    Ok(views.html.search.index(pager, filters, termfacets)(request))
  }
}