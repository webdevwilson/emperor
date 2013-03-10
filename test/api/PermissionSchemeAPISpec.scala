package test.api

import anorm._
import anorm.NotAssigned
import emp.JsonFormats._
import org.joda.time.DateTime
import org.specs2.mutable._
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

class PermissionSchemeAPISpec extends Specification {

  import models._

  "Permission Scheme API" should {

    "retrieve permission scheme" in {
      running(FakeApplication()) {

        val user = UserModel.getById(1).get

        val token = UserTokenModel.create(userId = user.id.get, comment = None)

        val tokenStr = "Token token=" + token.token.get

        val ps = PermissionScheme(name = "Test Permission Scheme", description = Some("Description"))
        val newPS = PermissionSchemeModel.create(ps)

        // Fetch a group by id
        val result1 = route(FakeRequest(GET, "/api/permission_scheme/" + newPS.id.get.toString).withHeaders("Authorization" -> tokenStr)).get
        status(result1) must equalTo(200)
        val apiPS = Json.fromJson[PermissionScheme](Json.parse(contentAsString(result1))).asOpt
        apiPS must beSome
        apiPS.get.name must beEqualTo(newPS.name)

        PermissionSchemeModel.delete(newPS.id.get)
      }
    }

    "modify and express permission scheme" in {
      running(FakeApplication()) {

        val user = UserModel.getById(1).get
        val group = GroupModel.getById(1).get
        val perm = PermissionSchemeModel.getPermissionById("PERM_TICKET_CREATE").get

        val token = UserTokenModel.create(userId = user.id.get, comment = None)

        val tokenStr = "Token token=" + token.token.get

        val ps = PermissionScheme(name = "Test Permission Scheme", description = Some("Description"))
        val newPS = PermissionSchemeModel.create(ps)

        // Fetch a group by id
        val result1 = route(FakeRequest(GET, "/api/permission_scheme/" + newPS.id.get.toString).withHeaders("Authorization" -> tokenStr)).get
        status(result1) must equalTo(200)
        val apiPS = Json.fromJson[PermissionScheme](Json.parse(contentAsString(result1))).asOpt
        apiPS must beSome
        apiPS.get.name must beEqualTo(newPS.name)

		// Add A User to the Scheme
        val result2 = route(FakeRequest(
        	PUT, "/api/permission_scheme/" + newPS.id.get + "/" + perm.name + "/user/" + user.id.get
        ).withHeaders("Authorization" -> tokenStr)).get
        status(result2) must equalTo(200)

		// Add A Group to the Scheme
        val result3 = route(FakeRequest(
        	PUT, "/api/permission_scheme/" + newPS.id.get + "/" + perm.name + "/group/" + group.id.get
        ).withHeaders("Authorization" -> tokenStr)).get
        status(result3) must equalTo(200)

        // Fetch Users
        val result4 = route(FakeRequest(
        	GET, "/api/permission_scheme/" + newPS.id.get + "/users/" + perm.name
        ).withHeaders("Authorization" -> tokenStr)).get
        status(result4) must equalTo(200)

        // Fetch Groups
        val result5 = route(FakeRequest(
        	GET, "/api/permission_scheme/" + newPS.id.get + "/groups/" + perm.name
        ).withHeaders("Authorization" -> tokenStr)).get
        status(result5) must equalTo(200)

		// Remove A User from the Scheme
        val result6 = route(FakeRequest(
        	DELETE, "/api/permission_scheme/" + newPS.id.get + "/" + perm.name + "/user/" + user.id.get
        ).withHeaders("Authorization" -> tokenStr)).get
        status(result6) must equalTo(200)
		contentAsString(result6)

		// Remove A Group from the Scheme
        val result7 = route(FakeRequest(
        	DELETE, "/api/permission_scheme/" + newPS.id.get + "/" + perm.name + "/group/" + group.id.get
        ).withHeaders("Authorization" -> tokenStr)).get
        status(result7) must equalTo(200)
		contentAsString(result7)

        PermissionSchemeModel.delete(newPS.id.get)
      }
    }
  }
}
