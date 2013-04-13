package test.api

import anorm._
import anorm.NotAssigned
import emp.JsonFormats._
import org.joda.time.DateTime
import org.specs2.mutable._
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

class GroupAPISpec extends Specification {

  import models._

  "Group API" should {

    "retrieve group" in {
      running(FakeApplication()) {

        val group = Group(name = "XXTest Group 1")

        val newGroup = GroupModel.create(group)

        val user = UserModel.getById(1).get

        val token = UserTokenModel.create(userId = user.id.get, comment = None)

        val tokenStr = "Token token=" + token.token.get

        // Fetch a group by id
        val result1 = route(FakeRequest(GET, "/api/group/" + newGroup.id.get).withHeaders("Authorization" -> tokenStr)).get
        status(result1) must equalTo(200)

        // Fetch all groups
        val result2 = route(FakeRequest(GET, "/api/group").withHeaders("Authorization" -> tokenStr)).get
        status(result2) must equalTo(200)

        // Starts with…
        val result3 = route(FakeRequest(GET, "/api/group/startswith?q=XX").withHeaders("Authorization" -> tokenStr)).get
        status(result3) must equalTo(200)
        contentAsString(result3) must contain("XXTest Group 1")

        UserTokenModel.delete(user.id.get, token.token.get)
        GroupModel.delete(newGroup.id.get)

        1 mustEqual(1)
      }
    }

    "handle membership changes" in {
      running(FakeApplication()) {

        val group = Group(name = "XXTest Group 1")

        val newGroup = GroupModel.create(group)

        val user = UserModel.getById(1).get
        val token = UserTokenModel.create(userId = user.id.get, comment = None)

        val tokenStr = "Token token=" + token.token.get

        val username = "testuser1"
        val iu = models.User(
          username = username,
          password = "1234",
          realName = "Test User",
          email    = "test@example.com",
          timezone = "America/Chicago"
        )
        val newUser = UserModel.create(iu)

        // Add A User
        val result1 = route(FakeRequest(PUT, "/api/group/" + newGroup.id.get + "/" + username).withHeaders("Authorization" -> tokenStr)).get
        status(result1) must equalTo(200)

        // Check for users
        val result2 = route(FakeRequest(GET, "/api/group/" + newGroup.id.get + "/users").withHeaders("Authorization" -> tokenStr)).get
        println(contentAsString(result2))
        contentAsString(result2) must contain(username)

        // Remove A User
        val result3 = route(FakeRequest(DELETE, "/api/group/" + newGroup.id.get + "/" + newUser.id.get).withHeaders("Authorization" -> tokenStr)).get
        status(result3) must equalTo(200)

        UserModel.delete(newUser.id.get)
        UserTokenModel.delete(user.id.get, token.token.get)
        GroupModel.delete(newGroup.id.get)

        1 mustEqual(1)
      }
    }

  }
}
