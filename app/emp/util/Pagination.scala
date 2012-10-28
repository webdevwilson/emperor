package emp.util

/**
 * Helper for pagination.
 */
object Pagination {

  /**
   * Convenient class that represents a single page out of a larger paginated
   * set of results. Contains smarts for preventing the request of pages
   * outside the bounds of the set.
   */
  case class Page[+A](items: Iterable[A], requestedPage: Int, count: Int, total: Long) {
    /**
     * The last page of the set.
     */
    lazy val lastPage = (total.toDouble / count).ceil.toInt
    /**
     * The current page.
     */
    lazy val page = requestedPage match {
        case p if p < firstPage => firstPage
        case p if p > lastPage => lastPage
        case _ => requestedPage
    }
    /**
     * Value of the page before the current one.
     */
    lazy val prev = Option(page - 1).filter(_ >= firstPage)
    /**
     * Value of the page after the current one.
     */
    lazy val next = Option(page + 1).filter(_ <= lastPage)
    /**
     * The first page. Always 1!
     */
    val firstPage = 1
    /**
     * Calculates an offset based on the `count` and the curent page that
     * is suitable for use with an SQL or search backend.
     */
    lazy val offset = page match {
      case p if p == 0 => 0
      case _ => count * (page - 1)
    }
  }
}