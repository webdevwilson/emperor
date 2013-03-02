package test

import anorm._
import anorm.NotAssigned
import org.joda.time.DateTime
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class TicketModelSpec extends Specification {

  import models._

  "Ticket model" should {

    "create, retrieve and delete ticket" in {
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

        val tp = TicketPriorityModel.getById(1).get
        val ts = TicketSeverityModel.getById(1).get
        val tt = TicketTypeModel.getById(1).get

        val t = models.Ticket(
          userId = user.id.get,
          projectId = newProject.id.get,
          priorityId = tp.id.get,
          severityId = ts.id.get,
          typeId = tt.id.get,
          summary = "Test Ticket 1"
        )
        val newTicket = TicketModel.create(userId = user.id.get, ticket = t)
        newTicket must beSome
        newTicket.get must beAnInstanceOf[models.FullTicket]
        newTicket.get.ticketId must beEqualTo("TEST1-1")

        val eTicket = TicketModel.getById(newTicket.get.ticketId)
        eTicket must beSome
        eTicket.get must beAnInstanceOf[models.EditTicket]

        TicketModel.delete(newTicket.get.ticketId)
        ProjectModel.delete(newProject.id.get)
        1 mustEqual(1)
      }
    }

    "handle comments" in {
      running(FakeApplication()) {

        val work = WorkflowModel.getById(1) // Assumes the default workflow exists

        val p = models.Project(
          name = "Test Project 2",
          key = "TEST2",
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

        val tp = TicketPriorityModel.getById(1).get
        val ts = TicketSeverityModel.getById(1).get
        val tt = TicketTypeModel.getById(1).get

        val t = models.InitialTicket(
          reporterId = user.id.get,
          projectId = newProject.id.get,
          priorityId = tp.id.get,
          severityId = ts.id.get,
          typeId = tt.id.get,
          summary = "Test Ticket 2"
        )
        val newTicket = TicketModel.create(userId = user.id.get, ticket = t)

        val comm = TicketModel.addComment(ticketId = newTicket.get.ticketId, ctype = "comment", userId = user.id.get, content = "Comment!")
        comm must beSome
        comm.get must beAnInstanceOf[models.Comment]

        val gcomm = TicketModel.getCommentById(comm.get.id.get)
        gcomm must beSome
        gcomm.get must beAnInstanceOf[models.Comment]

        TicketModel.deleteComment(comm.get.id.get)
        TicketModel.delete(newTicket.get.ticketId)
        ProjectModel.delete(newProject.id.get)
        1 mustEqual(1)
      }
    }

    // XXX gotta figure out the indexing thing here
    // "handle resolution & unresolution" in {
    //   running(FakeApplication()) {

    //     val work = WorkflowModel.getById(1) // Assumes the default workflow exists

    //     val p = models.Project(
    //       name = "Test Project 3",
    //       key = "TEST3",
    //       workflowId = work.get.id.get,
    //       dateCreated = new Date
    //     )
    //     val newProject = ProjectModel.create(p)

    //     val user = UserModel.getById(1).get
    //     val userId = user.id.get

    //     val tp = TicketPriorityModel.getById(1).get
    //     val tr = TicketResolutionModel.getById(1).get
    //     val ts = TicketSeverityModel.getById(1).get
    //     val tt = TicketTypeModel.getById(1).get

    //     val t = models.InitialTicket(
    //       reporterId = user.id.get,
    //       projectId = newProject.id.get,
    //       priorityId = tp.id.get,
    //       severityId = ts.id.get,
    //       typeId = tt.id.get,
    //       summary = "Test Ticket 3"
    //     )
    //     val newTicket = TicketModel.create(userId = user.id.get, ticket = t).get
    //     val ticketId = newTicket.ticketId
    //     newTicket.resolution.id must beNone

    //     TicketModel.resolve(ticketId = ticketId, userId = userId, resolutionId = tr.id.get, comment = None)
    //     val resolvedTicket = TicketModel.getFullById(ticketId).get
    //     resolvedTicket.resolution.id must beSome
    //     resolvedTicket.resolution.name must beSome
    //     resolvedTicket.resolution.id must beEqualTo(tr.id)

    //     TicketModel.delete(newTicket.ticketId)
    //     ProjectModel.delete(newProject.id.get)
    //     1 mustEqual(1)
    //   }
    // }

    "handle links" in {
      running(FakeApplication()) {

        val work = WorkflowModel.getById(1) // Assumes the default workflow exists

        val p = models.Project(
          name = "Test Project 4",
          key = "TEST4",
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

        val tp = TicketPriorityModel.getById(1).get
        val ts = TicketSeverityModel.getById(1).get
        val tt = TicketTypeModel.getById(1).get

        val t = models.InitialTicket(
          reporterId = user.id.get,
          projectId = newProject.id.get,
          priorityId = tp.id.get,
          severityId = ts.id.get,
          typeId = tt.id.get,
          summary = "Test Ticket 4"
        )
        val newTicket = TicketModel.create(userId = user.id.get, ticket = t)

        val t2 = models.InitialTicket(
          reporterId = user.id.get,
          projectId = newProject.id.get,
          priorityId = tp.id.get,
          severityId = ts.id.get,
          typeId = tt.id.get,
          summary = "Test Ticket 5"
        )
        val newTicket2 = TicketModel.create(userId = user.id.get, ticket = t2)

        val lt = TicketLinkTypeModel.getById(1)
        lt must beSome

        val link = TicketModel.link(linkTypeId = lt.get.id.get, parentId = newTicket.get.ticketId, childId = newTicket2.get.ticketId)
        link must beSome
        link.get.typeId must beEqualTo(lt.get.id.get)
        link.get.typeName must beEqualTo(lt.get.name)
        link.get.parentId must beEqualTo(newTicket.get.ticketId)
        link.get.parentSummary must beEqualTo(newTicket.get.summary)
        link.get.childId must beEqualTo(newTicket2.get.ticketId)
        link.get.childSummary must beEqualTo(newTicket2.get.summary)

        TicketModel.removeLink(link.get.id.get)
        TicketModel.delete(newTicket2.get.ticketId)
        TicketModel.delete(newTicket.get.ticketId)
        ProjectModel.delete(newProject.id.get)
        1 mustEqual(1)
      }
    }
  }
}
