package test

import anorm._
import anorm.NotAssigned
import java.util.Date
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class GroupModelSpec extends Specification {

  import models.{GroupModel,UserModel}

  "Group model" should {

    "create, retrieve, update and delete" in {
      running(FakeApplication()) {

        val group = models.Group(id = NotAssigned, name = "Test Group!", dateCreated = new Date())
        val newGroup = GroupModel.create(group)
        newGroup must beAnInstanceOf[models.Group]

        val retGroup = GroupModel.getById(newGroup.id.get)
        retGroup must beSome
        retGroup.get.name mustEqual group.name
        retGroup.get.dateCreated must beAnInstanceOf[Date]

        val cGroup = retGroup.get.copy(name = "Test Group 2!")
        val updatedGroup = GroupModel.update(cGroup.id.get, cGroup)
        updatedGroup must beSome
        updatedGroup.get.name mustEqual "Test Group 2!"

        val sgroups = GroupModel.getStartsWith("Test Group 2")
        sgroups.size mustEqual(1)

        GroupModel.delete(newGroup.id.get)
        val goneGroup = GroupModel.getById(newGroup.id.get)
        goneGroup must beNone
      }
    }

    "handle user management" in {
      running(FakeApplication()) {

        val group = models.Group(id = NotAssigned, name = "Test Group!", dateCreated = new Date())
        val newGroup = GroupModel.create(group)
        newGroup must beAnInstanceOf[models.Group]

        val iu = models.User(
          username = "testuser",
          password = "1234",
          realName = "Test User",
          email    = "test@example.com",
          dateCreated = new Date
        )
        val newUser = UserModel.create(iu)

        // Add user to the group
        GroupModel.addUser(userId = newUser.id.get, groupId = newGroup.id.get)

        // Get back a list of GroupUsers
        val groupUsers = GroupModel.getGroupUsersForUser(newUser.id.get)
        groupUsers.size mustEqual(1)

        // And a list of groups
        val groups = GroupModel.getForUser(newUser.id.get)
        groups.size mustEqual(1)

        // Remove the user
        GroupModel.removeUser(userId = newUser.id.get, groupId = newGroup.id.get)
        val goneUsers = GroupModel.getGroupUsersForUser(newUser.id.get)
        goneUsers.size mustEqual(0)

        // Clean up
        UserModel.delete(newUser.id.get)
        GroupModel.delete(newGroup.id.get)

        1 mustEqual(1)
      }
    }
  }
}