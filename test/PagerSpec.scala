package test

import org.specs2.mutable._
import play.api.test._

class PagerSpec extends Specification {

  import emp._

  "Page" should {

    "return sane results for first page" in {

      val pager = Page(
        items = List(1,2,3,4,5),
        requestedPage = 1,
        count = 2,
        total = 10
      )

      pager.offset mustEqual 0
      pager.firstPage mustEqual 1
      pager.lastPage mustEqual 5
      pager.prev must beNone
      pager.next must beSome
      pager.next.get mustEqual 2
    }

    "return sane results for mid-pages" in {

      val pager = Page(
        items = List(1,2,3,4,5),
        requestedPage = 2,
        count = 2,
        total = 10
      )

      pager.offset mustEqual 2
      pager.prev must beSome
      pager.prev.get mustEqual 1
      pager.next must beSome
      pager.next.get mustEqual 3
    }

    "return sane results for last page" in {

      val pager = Page(
        items = List(1,2,3,4,5),
        requestedPage = 5,
        count = 2,
        total = 10
      )

      pager.offset mustEqual 8
      pager.prev must beSome
      pager.prev.get mustEqual 4
      pager.next must beNone
    }

    "return sane results for out of bounds page (high)" in {

      val pager = Page(
        items = List(1,2,3,4,5),
        requestedPage = 6,
        count = 2,
        total = 10
      )

      pager.page mustEqual 5
      pager.offset mustEqual 8
      pager.prev must beSome
      pager.prev.get mustEqual 4
      pager.next must beNone
    }

    "return sane results for out of bounds page (low)" in {

      val pager = Page(
        items = List(1,2,3,4,5),
        requestedPage = -6,
        count = 2,
        total = 10
      )

      pager.page mustEqual 1
      pager.offset mustEqual 0
      pager.prev must beNone
      pager.next must beSome
      pager.next.get mustEqual 2
    }

    "creates proper pager links" in {

      Library.pagerLink(FakeRequest(), 1, 10) mustEqual "/?page=1&count=10&"
      Library.pagerLink(FakeRequest(), 2, 4) mustEqual "/?page=2&count=4&"
      Library.pagerLink(FakeRequest()) mustEqual "/?page=1&count=10&"
      Library.pagerLink(request = FakeRequest(), count = 5) mustEqual "/?page=1&count=5&"
    }
  }
}