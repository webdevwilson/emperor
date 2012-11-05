package test

import anorm._
import anorm.NotAssigned
import org.joda.time.DateTime
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class UserModelSpec extends Specification {

  import models.UserModel

  "User model" should {

    "create, retrieve and delete" in {
      running(FakeApplication()) {

        val iu = models.User(
          username = "testuser1",
          password = "1234",
          realName = "Test User",
          email    = "test@example.com",
          dateCreated = new Date
        )
        val newUser = UserModel.create(iu)
        newUser must beAnInstanceOf[models.User]

        // Get it by id
        val user = UserModel.getById(newUser.id.get)
        user must beSome
        user.get must beAnInstanceOf[models.User]

        // // Change it
        val cUser = models.User(
          username = "testuser1",
          password = "1234",
          realName = "Testy User",
          email = "test@example.com",
          dateCreated = new Date
        )
        val updatedUser = UserModel.update(newUser.id.get, cUser)
        updatedUser must beSome
        updatedUser.get must beAnInstanceOf[models.User]
        updatedUser.get.realName mustEqual "Testy User"

        // Nix it
        UserModel.delete(newUser.id.get)
        val goneUser =  UserModel.getById(newUser.id.get)
        goneUser must beNone
      }
    }
  }
}