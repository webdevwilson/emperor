package test

import anorm._
import anorm.NotAssigned
import java.util.Date
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class TicketModelSpec extends Specification {

  import models.{ProjectModel,TicketModel,TicketPriorityModel,TicketSeverityModel,TicketTypeModel,UserModel,WorkflowModel}

  "Ticket model" should {

    "create, retrieve and delete" in {
      running(FakeApplication()) {

        val work = WorkflowModel.getById(1) // Assumes the default workflow exists

        val p = models.Project(
          name = "Test Project 1",
          key = "TEST1",
          workflowId = work.get.id.get,
          dateCreated = new Date
        )
        val newProject = ProjectModel.create(p)

        val user = UserModel.getById(1).get

        val tp = TicketPriorityModel.getById(1).get
        val ts = TicketSeverityModel.getById(1).get
        val tt = TicketTypeModel.getById(1).get

        val t = models.InitialTicket(
          reporterId = user.id.get,
          projectId = newProject.id.get,
          priorityId = tp.id.get,
          severityId = ts.id.get,
          typeId = tt.id.get,
          summary = "Test Project 1"
        )
        val newTicket = TicketModel.create(userId = user.id.get, ticket = t)
        newTicket must beSome
        newTicket.get must beAnInstanceOf[models.FullTicket]

        TicketModel.delete(newTicket.get.id.get)
        ProjectModel.delete(newProject.id.get)
        1 mustEqual(1)
      }
    }
  }
}
