package test

import anorm._
import anorm.NotAssigned
import java.util.Date
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class GroupModelSpec extends Specification with After {

  import models.GroupModel

  "Group model" should {

    "create and retrieve" in {
      running(FakeApplication()) {

        val group = models.Group(id = NotAssigned, name = "Test Group!", dateCreated = new Date())
        val newGroup = GroupModel.create(group)
        newGroup must beAnInstanceOf[models.Group]

        val retGroup = GroupModel.getById(newGroup.id.get)
        retGroup must beSome
        retGroup.get.name mustEqual group.name
        retGroup.get.dateCreated must beAnInstanceOf[Date]
      }
    }

    "foobar" in {
      1 mustEqual 1
    }
  }

  override def after {
    println("WAKKA WAKKA")
  }
}