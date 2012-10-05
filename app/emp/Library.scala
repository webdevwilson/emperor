package emp

import collection.JavaConversions._
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.facet.terms.strings._
import org.elasticsearch.search.facet.terms.longs.InternalLongTermsFacet
import play.api.mvc._
import scala.collection.mutable.ListBuffer

/**
 * Helper for pagination.
 */
case class Page[+A](items: Iterable[A], requestedPage: Int, count: Int, total: Long) {
  lazy val lastPage = (total.toDouble / count).ceil.toInt
  lazy val page = requestedPage match {
      case p if p < firstPage => firstPage
      case p if p > lastPage => lastPage
      case _ => requestedPage
  }
  lazy val prev = Option(page - 1).filter(_ >= firstPage)
  lazy val next = Option(page + 1).filter(_ <= lastPage)
  val firstPage = 1
  lazy val offset = page match {
    case p if p == 0 => 0
    case _ => count * (page - 1)
  }
}

case class Facet(value: String, count: Long)
case class Facets(name: String, items: Seq[Facet])
case class SearchResult[A](pager: Page[A], facets: Seq[Facets])

object Library {

  def pagerLink(request: Request[AnyContent], page: Int = 1, count: Int = 10) : String = {

    var q = request.queryString
    q += "page" -> List(page.toString)
    q += "count" -> List(count.toString)

    val qs = q.foldLeft("")(
      (acc, value) => acc + value._2.foldLeft("")(
        (acc2, param) => acc2 + value._1 + "=" + param + "&"
      )
    )

    request.path + "?" + qs
  }

  /**
   * Return a String (URL) with a query parameter (`name`) and `value`.
   * It uses the request to populate existing query params and also
   * to provide the base query path.
   * To override the base path you can pass in a `path`.
   */
  def filterLink(request: Request[AnyContent], path: Option[String] = None, name: String, value: String): String = {

    var q = request.queryString
    q += name -> List(value)

    // Filter out any empty values
    val clean = q.filterNot { p =>
      val vals = p._2.filter { v => v != "" }
      vals.isEmpty
    }

    val qs = clean.foldLeft("")(
      (acc, value) => acc + value._2.foldLeft("")(
        (acc2, param) => {
          value._1 match {
            case "page" => ""
            case _ => acc2 + value._1 + "=" + param + "&"
          }
        }
      )
    )

    path match {
      case Some(p) => p + "?" + qs
      case None => request.path + "?" + qs
    }
  }

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
}
