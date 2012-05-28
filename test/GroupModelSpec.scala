package test

import anorm._
import anorm.NotAssigned
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class GroupModelSpec extends Specification {
    
  import models.GroupModel
    
  "Group model" should {
        
    "create and retrieve" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        val group = models.Group(NotAssigned, "Test Group!")
        val newGroup = GroupModel.create(group)
        println(newGroup.id)

        val retGroup = GroupModel.getById(newGroup.id.get)
        retGroup must beSome
        retGroup.get.name mustEqual group.name
        1 mustEqual 1
      }
    }
  }
}