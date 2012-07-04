package chc

import play.api.mvc._
import scala.collection.mutable.ListBuffer

/**
 * Helper for pagination.
 */
case class Page[+A](items: Seq[A], requestedPage: Int, count: Int, total: Long) {
  lazy val lastPage = (total.toDouble / count).ceil.toInt
  lazy val page = {
    if(requestedPage > this.lastPage) {
      this.lastPage
    } else {
      requestedPage
    }
  }
  lazy val prev = Option(page - 1).filter(_ >= 1)
  lazy val next = Option(page + 1).filter(_ => ((count * (page - 1)) + items.size) < total)
  val firstPage = 0
  lazy val offset = count * (page - 1)
}

case class Facet(name: String, value: String, count: Long)
case class Facets(name: String, param: String, items: Seq[Facet])
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

  def filterLink(request: Request[AnyContent], name: String, value: String) : String = {

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

    request.path + "?" + qs
  }
}
