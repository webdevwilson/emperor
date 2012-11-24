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
     * The range of pages.
     */
     lazy val range = Range(start = 1, end = 1.to(total.toInt).by(count).size + 1)
    /**
     * The current page.
     */
    lazy val page = requestedPage match {
        case p if p < range.start => range.end
        case p if p > range.end => range.end
        case _ => requestedPage
    }
    /**
     * Value of the page before the current one.
     */
    lazy val prev = Option(page - 1).filter(_ >= range.start)
    /**
     * Value of the page after the current one.
     */
    lazy val next = Option(page + 1).filter(_ <= range.end)

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