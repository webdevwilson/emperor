package controllers

import chc._
import collection.JavaConversions._
import models.SearchModel
import org.elasticsearch.search.facet.terms.strings._
import play.api.mvc._

object Search extends Controller with Secured {

  def index(page: Int, count: Int, query: String) = IsAuthenticated { implicit request =>

    val response = SearchModel.searchTicket(page, count, query)

    val pager = Page(response.hits.hits, page, count, response.hits.totalHits)
    
    // The response contains a list of facets, but they are generic'ed down
    // to Facet rather than their real fucking class.  Therefore we re-cast
    // them all in this list and filter any that do not have more than one
    // value.
    val termfacets = response.facets.facets.map { facet =>
      facet match {
        case t: InternalStringTermsFacet => t
      }
    } filter { f => f.entries.size > 1 }
    println(termfacets)

    Ok(views.html.search.index(pager, termfacets)(request))
  }
}