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

    "parse and validate string ids" in {

      TicketModel.isValidTicketId("FOO-1") must beTrue
      TicketModel.isValidTicketId("-1") must beFalse

      TicketModel.parseTicketId("FOO-1") must beSome
      TicketModel.parseTicketId("123") must beNone
    }

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

        val newTicket = TicketModel.create(
          userId = user.id.get,
          projectId = newProject.id.get,
          typeId = tt.id.get,
          priorityId = tp.id.get,
          severityId = ts.id.get,
          summary = "Test Ticket 1"
        )
        newTicket must beSome
        newTicket.get must beAnInstanceOf[models.FullTicket]
        newTicket.get.ticketId must beEqualTo("TEST1-1")

        val eTicket = TicketModel.getById(newTicket.get.id.get)
        eTicket must beSome
        eTicket.get must beAnInstanceOf[models.Ticket]

        val efTicket = TicketModel.getFullById(newTicket.get.id.get)
        efTicket must beSome
        efTicket.get must beAnInstanceOf[models.FullTicket]

        val sTicket = TicketModel.getByStringId(newTicket.get.ticketId)
        sTicket must beSome
        sTicket.get must beAnInstanceOf[models.Ticket]

        val sfTicket = TicketModel.getFullByStringId(newTicket.get.ticketId)
        sfTicket must beSome
        sfTicket.get must beAnInstanceOf[models.FullTicket]

        val ticketData = TicketModel.getDataById(newTicket.get.id.get)
        ticketData must beSome
        ticketData.get must beAnInstanceOf[models.TicketData]

        TicketModel.delete(newTicket.get.id.get)
        ProjectModel.delete(newProject.id.get)
        1 mustEqual(1)
      }
    }

    "provide convenience methods for string id operations" in {
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

        val newTicket = TicketModel.create(
          userId = user.id.get,
          projectId = newProject.id.get,
          typeId = tt.id.get,
          priorityId = tp.id.get,
          severityId = ts.id.get,
          summary = "Test Ticket 1"
        )
        newTicket must beSome

        TicketModel.getActualId(newTicket.get.ticketId).get must beEqualTo(newTicket.get.id.get)

        TicketModel.delete(newTicket.get.id.get)
        ProjectModel.delete(newProject.id.get)
        1 mustEqual(1)
      }
    }

    "handle assignment" in {
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

        val newTicket = TicketModel.create(
          userId = user.id.get,
          projectId = newProject.id.get,
          typeId = tt.id.get,
          priorityId = tp.id.get,
          severityId = ts.id.get,
          summary = "Test Ticket 1"
        )

        newTicket must beSome
        newTicket.get.assignee.id must beNone

        val assignedTicket = TicketModel.assign(
          ticketId = newTicket.get.id.get,
          userId = user.id.get,
          assigneeId = Some(user.id.get)
        )
        assignedTicket.assignee.id must beSome
        assignedTicket.assignee.id.get must beEqualTo(user.id.get)

        TicketModel.delete(newTicket.get.id.get)
        ProjectModel.delete(newProject.id.get)
        1 mustEqual(1)
      }
    }

    "handle resolution" in {
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

        val newTicket = TicketModel.create(
          userId = user.id.get,
          projectId = newProject.id.get,
          typeId = tt.id.get,
          priorityId = tp.id.get,
          severityId = ts.id.get,
          summary = "Test Ticket 1"
        )

        newTicket must beSome
        newTicket.get.assignee.id must beNone

        val reso = TicketResolutionModel.getById(1)

        // Resolve it!
        val assignedTicket = TicketModel.resolve(
          ticketId = newTicket.get.id.get,
          userId = user.id.get,
          resolutionId = reso.get.id.get
        )
        assignedTicket.resolution.id must beSome
        assignedTicket.resolution.id.get must beEqualTo(reso.get.id.get)

        // Unresolve it!
        val unassignedTicket = TicketModel.unresolve(
          ticketId = newTicket.get.id.get,
          userId = user.id.get
        )
        unassignedTicket.resolution.id must beNone

        TicketModel.delete(newTicket.get.id.get)
        ProjectModel.delete(newProject.id.get)
        1 mustEqual(1)
      }
    }

    "handle status change" in {
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

        val newTicket = TicketModel.create(
          userId = user.id.get,
          projectId = newProject.id.get,
          typeId = tt.id.get,
          priorityId = tp.id.get,
          severityId = ts.id.get,
          summary = "Test Ticket 1"
        )

        newTicket must beSome
        newTicket.get.assignee.id must beNone

        val status = TicketStatusModel.getById(3)

        // Resolve it!
        val changedTicket = TicketModel.changeStatus(
          ticketId = newTicket.get.id.get,
          userId = user.id.get,
          newStatusId = status.get.id.get
        )
        changedTicket.status.id must beEqualTo(status.get.id.get)

        TicketModel.delete(newTicket.get.id.get)
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

        val newTicket = TicketModel.create(
          userId = user.id.get,
          projectId = newProject.id.get,
          priorityId = tp.id.get,
          severityId = ts.id.get,
          typeId = tt.id.get,
          summary = "Test Ticket 2"
        )

        val comm = TicketModel.addComment(ticketId = newTicket.get.id.get, ctype = "comment", userId = user.id.get, content = "Comment!")
        comm must beSome
        comm.get must beAnInstanceOf[models.Comment]

        val gcomm = TicketModel.getCommentById(comm.get.id.get)
        gcomm must beSome
        gcomm.get must beAnInstanceOf[models.Comment]

        TicketModel.deleteComment(comm.get.id.get)
        TicketModel.delete(newTicket.get.id.get)
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

        val newTicket = TicketModel.create(
          userId = user.id.get,
          projectId = newProject.id.get,
          priorityId = tp.id.get,
          severityId = ts.id.get,
          typeId = tt.id.get,
          summary = "Test Ticket 4"
        )
        newTicket must beSome

        val newTicket2 = TicketModel.create(
          userId = user.id.get,
          projectId = newProject.id.get,
          priorityId = tp.id.get,
          severityId = ts.id.get,
          typeId = tt.id.get,
          summary = "Test Ticket 5"
        )
        newTicket2 must beSome

        val lt = TicketLinkTypeModel.getById(1)
        lt must beSome

        val link = TicketModel.link(
          linkTypeId = lt.get.id.get, parentId = newTicket.get.id.get, childId = newTicket2.get.id.get
        )
        link must beSome
        link.get.typeId must beEqualTo(lt.get.id.get)
        link.get.typeName must beEqualTo(lt.get.name)
        link.get.parentId must beEqualTo(newTicket.get.id.get)
        link.get.parentSummary must beEqualTo(newTicket.get.summary)
        link.get.childId must beEqualTo(newTicket2.get.id.get)
        link.get.childSummary must beEqualTo(newTicket2.get.summary)

        val links = TicketModel.getLinks(newTicket.get.id.get)
        links must have size(1)
        links(0).id.get must beEqualTo(link.get.id.get)

        TicketModel.removeLink(link.get.id.get)
        TicketModel.delete(newTicket2.get.id.get)
        TicketModel.delete(newTicket.get.id.get)
        ProjectModel.delete(newProject.id.get)
        1 mustEqual(1)
      }
    }
  }
}
