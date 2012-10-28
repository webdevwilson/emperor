package emp.util

import models.{ProjectModel,SearchModel}
import collection.JavaConversions._
import com.traackr.scalastic.elasticsearch.Indexer
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query._
import org.elasticsearch.index.query.FilterBuilders._
import org.elasticsearch.index.query.QueryBuilders._
import org.elasticsearch.search.facet.terms.strings._
import org.elasticsearch.search.facet.terms.longs.InternalLongTermsFacet
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.facet.FacetBuilders._
import org.elasticsearch.search.sort._
import play.api.Logger
import emp.util.Pagination.Page

/**
 * Utilities for search.
 *?
object Search {

  /**
   * Class for search queries
   */
  case class SearchQuery(
    userId: Long,
    page: Int = 1,
    count: Int = 10,
    query: String = "",
    /**
     * Map of filters.  Key is the name.
     */
    filters: Map[String, Seq[String]] = Map.empty,
    sortBy: Option[String] = Some("date_created"),
    sortOrder: Option[String] = None
  )

  /**
   * A facet of a search.
   */
  case class Facet(value: String, count: Long)

  /**
   * A collection of facets.
   */
  case class Facets(name: String, items: Seq[Facet])

  /**
   * A search result.
   */
  case class SearchResult[A](pager: Page[A], facets: Seq[Facets])

  /**
   * Transform an ElasticSearch SearchResponse into a SearchResult.  Contains
   * logic for converting ES' crazy Facet classes into something sane.
   */
  def parseSearchResponse[A](pager: Page[A], response: SearchResponse): SearchResult[A] = {

    val facets = response.facets.facets map { facet =>
      Facets(
        name  = facet.getName,
        items = facet match {
          case t: InternalStringTermsFacet => {
            t.entries map { fitem =>
              Facet(
                value = fitem.getTerm,
                count = fitem.getCount
              )
            }
          }
          case t: InternalLongTermsFacet => {
            t.entries map { fitem =>
              Facet(
                value = fitem.getTerm,
                count = fitem.getCount
              )
            }
          }
        }
      )
    } filter { f => f.items.size > 1 } // Eliminate facets with only one item

    SearchResult(pager = pager, facets = facets)
  }

  // XXX this could be abstracted if we pre-did the project id stuff…
  /**
   * Run a query against ElasticSearch. Creates a boolean filter query that
   * wraps a project limiting filter and the the supplied filters.
   *
   * It's worth mentioning that this method will strip filters for `project_id`
   * <b>out</b> of the filters supplied in `query`.  If `filterProjects` is true
   * it will use the user in `query` to create a filter of visible projects. See
   * `getVisibileProjects` in [[models.ProjectModel]].
   */
  def runQuery(indexer: Indexer, index: String, query: SearchQuery, filterMap: Map[String,String], sortMap: Map[String,String], facets: Map[String,String] = Map.empty, filterProjects: Boolean = true): SearchResponse = {

    // Make a bool filter to collect all our filters together
    val finalFilter: BoolFilterBuilder = boolFilter

    val termFilters : Iterable[Seq[FilterBuilder]] = query.filters.filter { kv =>
      kv._1 != "project_id" && filterMap.get(kv._1).isDefined
    } map {
      case (key, values) => values.map { v =>
        termFilter(filterMap.get(key).getOrElse(key), v).asInstanceOf[FilterBuilder]
      }
    }

    // There are some rare cases where we don't want the project filter added.
    if(filterProjects) {
      // Get the projects this user can see
      val pids = ProjectModel.getVisibleProjectIds(query.userId).map { p => p.toString }

      val finalPids = {
        // If the user supplied a project id (or the app did on the users behalf,
        // whatever) intersect it with the list we got from permissions.
        query.filters.get("project_id").map({ upids => pids.intersect(upids) }).getOrElse(pids)
      }

      // Definitely going to have a project filter, everyone does
      val projFilter = orFilter(finalPids.map { pid => termFilter("project_id", pid).asInstanceOf[FilterBuilder] }:_*)
      // Add this to our bool filter
      finalFilter.must(projFilter)
    }

    // Might not have user filters
    if(!termFilters.isEmpty) {
      val userFilter = andFilter(termFilters.flatten.toSeq:_*)
      finalFilter.must(userFilter)
    }
    val actualQuery = filteredQuery(queryString(if(query.query.isEmpty) "*" else query.query), finalFilter)

    Logger.debug("Running ES query:")
    Logger.debug(actualQuery.toString)

    val sortOrder = query.sortOrder match {
      case Some(s) => if(s.equalsIgnoreCase("desc")) SortOrder.DESC else SortOrder.ASC
      case None => SortOrder.DESC
    }

    indexer.search(
      query = actualQuery,
      indices = Seq(index),
      facets = facets.map { case (name, field) =>
        termsFacet(name).field(field)
      },
      size = Some(query.count),
      from = query.page match {
        case 0 => Some(0)
        case 1 => Some(0)
        case _ => Some((query.page - 1) * query.count)
      },
      sorting = Seq(
        // This is a bit messy… but it gets the job done.
        sortMap.get(query.sortBy.getOrElse("date_created")).getOrElse("date_created") -> sortOrder
      )
    )
  }
}