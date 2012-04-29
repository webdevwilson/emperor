package test

import org.specs2.mutable._
import play.api.test._

class PagerSpec extends Specification {
    
  import chc._
    
  "Page" should {

    "return sane results for first page" in {

      val pager = Page(List(1,2,3,4,5), 0, 2, 10)

      pager.offset mustEqual 0
      pager.prev must beNone
      pager.next must beSome
      pager.next.get mustEqual 1
      pager.firstPage mustEqual 0
      pager.lastPage mustEqual 4
    }
   
    "return sane results for mid-pages" in {
    
      val pager = Page(List(1,2,3,4,5), 1, 2, 10)

      pager.offset mustEqual 2
      pager.prev must beSome
      pager.prev.get mustEqual 0
      pager.next must beSome
      pager.next.get mustEqual 2
    }

    "return sane results for last page" in {

      val pager = Page(List(1,2,3,4,5), 4, 2, 10)

      pager.offset mustEqual 8
      pager.prev must beSome
      pager.prev.get mustEqual 3
      pager.next must beNone
    }
    
    "creates proper pager links" in {
      
      Library.pagerLink(FakeRequest(), 0, 10) mustEqual "/?page=0&count=10&"
      Library.pagerLink(FakeRequest(), 2, 4) mustEqual "/?page=2&count=4&"
      Library.pagerLink(FakeRequest()) mustEqual "/?page=0&count=10&"
      Library.pagerLink(request = FakeRequest(), count = 5) mustEqual "/?page=0&count=5&"
    }
  }
}