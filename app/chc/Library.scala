package chc

import play.api.mvc._
import scala.collection.mutable.ListBuffer

/**
 * Helper for pagination.
 */
case class Page[+A](items: Seq[A], page: Int, count: Int, total: Long) {
  lazy val offset = count * page
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => ((count * page) + items.size) < total)
  val firstPage = 0
  lazy val lastPage = (total.toDouble / count).ceil.toInt - 1
}

case class Facet(name: String, value: String, count: Int)
case class Facets(name: String, param: String, items: Seq[Facet])
case class SearchResult[A](pager: Page[A], facets: Seq[Facets])

object Library {
    
  def pagerLink(request: Request[AnyContent], page: Int = 0, count: Int = 10) : String = {

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
}
