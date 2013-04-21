package emp.util

import collection.JavaConversions._
import emp.util.Pagination.Page
import models.{ProjectModel,SearchModel}
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
import play.api.libs.json.Json
import scala.concurrent.Future
import scalastic.elasticsearch._, SearchParameterTypes._
import wabisabi.Client

/**
 * Utilities for search.
 */
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
    parser: String = "text",
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
  def parseSearchResponse[A](pager: Page[A], response: String): SearchResult[A] = {

    // val jason = Await.result(response, Duration(1, "seconds"))
    // val jsonTrans = (__ \ "hits").json.pick[List[Comment]]
    // val hits = Json.parse(jason).transform(jsonTrans)
    // val hits = (Json.parse(jason) \ "hits" \ "hits" \\ "_source").map({ h => Json.fromJson[](h) })

    // val pager = Page(hits, 1, 1, 1)
    // val pager = Page(hits, query.page, query.count, hits.totalHits)

    // val facets = response.facets.facets map { facet =>
    //   Facets(
    //     name  = facet.getName,
    //     items = facet match {
    //       case t: InternalStringTermsFacet => {
    //         t.entries map { fitem =>
    //           Facet(
    //             value = fitem.getTerm,
    //             count = fitem.getCount
    //           )
    //         }
    //       }
    //       case t: InternalLongTermsFacet => {
    //         t.entries map { fitem =>
    //           Facet(
    //             value = fitem.getTerm,
    //             count = fitem.getCount
    //           )
    //         }
    //       }
    //     }
    //   )
    // } filter { f => f.items.size > 1 } // Eliminate facets with only one item

    SearchResult(pager = pager, facets = Seq())
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
  def runQuery(
    client: Client, index: String, query: SearchQuery, filterMap: Map[String,String],
    sortMap: Map[String,String], facets: Map[String,String] = Map.empty,
    filterProjects: Boolean = true
  ): Future[String] = {

    val termFilters: Option[FilterBuilder] = if(query.filters.nonEmpty) {

      // Filter out filter keys of project_id or keys that do not have
      // and entry in the filterMap, which is a list of user-facing keys to
      // index fields.
      val valid = query.filters.filter({ kv =>
        kv._1 != "project_id" && filterMap.get(kv._1).isDefined
      })
      if(valid.nonEmpty) {
        val afilt = andFilter()
        valid.foreach({ kv =>
          kv._2.foreach({ v =>
            afilt.add(
              // Create a term filter for each supplied filter (and value)
              // Use the filter map to turn a passed in field name into an indexed
              // field name.  We can use get because we verified in the filter that
              // each key was present.
              termFilter(filterMap.get(kv._1).get, v)
            )
          })
        })
        Some(afilt)
      } else {
        None
      }
    } else {
      None
    }

    // There are some rare cases where we don't want the project filter added.
    val projFilters = if(filterProjects) {
      // Get the projects this user can see
      val pids = ProjectModel.getVisibleProjectIds(query.userId).map { p => p.toString }

      val finalPids = {
        // If the user supplied a project id (or the app did on the users behalf,
        // whatever) intersect it with the list we got from permissions.
        query.filters.get("project_id").map({ upids => pids.intersect(upids) }).getOrElse(pids)
      }

      // Definitely going to have a project filter, everyone does
      if(finalPids.nonEmpty) {
        Some(orFilter(finalPids.map { pid => termFilter("project_id", pid).asInstanceOf[FilterBuilder] }:_*))
      } else {
        None
      }
    } else {
      None
    }

    // Allow the user to choose between the query_string parser and the
    // match_phase_prefix parser.  The former despises hyphens, making ID
    // searches go to shit.  The latter doesn't support ranges and boolean
    // stuff.
    val qq = query.parser match {
      case "match" => matchPhrasePrefixQuery("_all", query.query)
      case _ => queryString(if(query.query.isEmpty) "*" else query.query)
    }

    val actualQuery = if(termFilters.isDefined || projFilters.isDefined) {
      val finalFilter: BoolFilterBuilder = boolFilter
      if(termFilters.isDefined) finalFilter.must(termFilters.get)
      if(projFilters.isDefined) finalFilter.must(projFilters.get)
      filteredQuery(qq, finalFilter)
    } else {
      qq
    }

    // Make a bool filter to collect all our filters together
    // val finalFilter: BoolFilterBuilder = boolFilter

    // Might not have user filters
    // if(!termFilters.isEmpty) {
    //   val userFilter = andFilter(termFilters.flatten.toSeq:_*)
    //   finalFilter.must(userFilter)
    // }
    // val actualQuery = filteredQuery(queryString(if(query.query.isEmpty) "*" else query.query), finalFilter)

    Logger.debug("Running ES query against index " + index + ":")
    Logger.debug(actualQuery.toString)

    val sortOrder = query.sortOrder match {
      case Some(s) => if(s.equalsIgnoreCase("desc")) SortOrder.DESC else SortOrder.ASC
      case None => SortOrder.DESC
    }

    val fullSearch = Json.obj(
      "query" -> "{}", //Json.parse(actualQuery.toString),
      "from" -> 0, //query.page match {
      //   case 0 => 0
      //   case 1 => 0
      //   case _ => (query.page - 1) * query.count
      // },
      "size" -> query.count
    )

    client.search(index = index, query = fullSearch.toString)
      // query = actualQuery,
      // indices = Seq(index),
      // facets = facets.map { case (name, field) =>
      //   termsFacet(name).field(field)
      // },
      // size = Some(query.count),
      // from = ,
      // sortings = Seq(
      //   // This is a bit messy… but it gets the job done.
      //   FieldSort(field = sortMap.get(query.sortBy.getOrElse("date_created")).getOrElse("date_created"), order = sortOrder)
      // )
    // )
  }
}