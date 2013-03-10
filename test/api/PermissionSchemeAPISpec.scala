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
  }
}
