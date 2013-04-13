package test.api

import anorm._
import anorm.NotAssigned
import emp.JsonFormats._
import org.joda.time.DateTime
import org.specs2.mutable._
import play.api.libs.json._
// import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

class TicketAPISpec extends Specification {

  import models._

  "Ticket API" should {

    "create ticket" in {
      running(FakeApplication()) {

        val work = WorkflowModel.getById(1) // Assumes the default workflow exists

        val p = models.Project(
          name = "Test Project 1",
          key = "TEST1",
          workflowId = work.get.id.get,
          ownerId = None,
          permissionSchemeId = 1,
          defaultPriorityId = None,
          defaultSeverityId = None,
          defaultTypeId = None,
          defaultAssignee = None,
          dateCreated = new DateTime
        )
        val newProject = ProjectModel.create(p).get

        val user = UserModel.getById(1).get

        val token = UserTokenModel.create(userId = user.id.get, comment = None)

        val tokenStr = "Token token=" + token.token.get

        // Assume we have the default ticket severities
        val result = route(FakeRequest(
          POST,
          "/api/project/" + newProject.id.get.toString + "/ticket",
          FakeHeaders(),
          Json.toJson(Map(
            "typeId" -> JsNumber(1), // Assumes default types are present
            "priorityId" -> JsNumber(1), // ^
            "severityId" -> JsNumber(1), // ^
            "summary" -> JsString("A ticket!")
          ))
        ).withHeaders("Authorization" -> tokenStr)).get
        status(result) must equalTo(200)

        println(contentAsString(result))
        val apiTick = Json.fromJson[FullTicket](Json.parse(contentAsString(result))).asOpt
        apiTick must beSome
        apiTick.get.summary must beEqualTo("A ticket!")

        TicketModel.delete(apiTick.get.id.get)
        ProjectModel.delete(newProject.id.get)
        UserTokenModel.delete(user.id.get, token.token.get)
        1 mustEqual(1)
      }
    }
  }
}