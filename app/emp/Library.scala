package emp

import collection.JavaConversions._
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

object Library {

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

    path.map({ p => p + "?" + qs }).getOrElse(request.path + "?" + qs)
  }

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

  def sortLink(request: Request[AnyContent], name: String): String = {

    // Get rid of sort and order, we'll re-set those
    val cleanQ: Map[String,Seq[String]] = request.queryString.filterKeys { key => !key.equalsIgnoreCase("sort") && !key.equalsIgnoreCase("order") }

    val order = request.queryString.get("order") match {
      case Some(v) if v.first.isEmpty => "desc"
      case Some(v) if v.first.equalsIgnoreCase("desc") => "asc"
      case _ => "desc"
    }
    val q = cleanQ ++ Map("sort" -> Seq(name), "order" -> Seq(order))
    // val q: Map[String,Seq[String]] = request.queryString.map { case (key, value) =>
    //   key match {
    //     case n if n.equalsIgnoreCase("sort") => (key -> Seq(name))
    //     case n if n.equalsIgnoreCase("order") => {
    //       val newsort = if(value.isEmpty) {
    //         "desc"
    //       } else if(value.first.equalsIgnoreCase("desc")) {
    //         "asc"
    //       } else {
    //         "desc"
    //       }
    //       ("order" -> Seq(newsort))
    //     }
    //     case _ => (key -> value)
    //   }
    // }

    val qs = q.foldLeft("")(
      (acc, value) => acc + value._2.foldLeft("")(
        (acc2, param) => acc2 + value._1 + "=" + param + "&"
      )
    )

    request.path + "?" + qs
  }
}
