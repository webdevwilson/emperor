package emp.util

object Pagination {
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
}