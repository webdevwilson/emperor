package test

import anorm._
import anorm.NotAssigned
import java.util.Date
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class PermissionSchemeModelSpec extends Specification {

  import models._

  "PermissionScheme model" should {

    "create, retrieve and delete" in {
      running(FakeApplication()) {

        val psObj = models.PermissionScheme(
          name = "Test Permission Schema",
          description = Some("Testing!"),
          dateCreated = new Date
        )
        val newPs = PermissionSchemeModel.create(psObj)
        newPs must beAnInstanceOf[models.PermissionScheme]

        val pses = PermissionSchemeModel.getAll
        pses.isEmpty must beFalse

        // Get it by id
        val ps = PermissionSchemeModel.getById(newPs.id.get)
        ps must beSome
        ps.get must beAnInstanceOf[models.PermissionScheme]

        // // Change it
        val cPs = ps.get.copy(name = "Test Permission Scheme 1!")
        val updatePs = PermissionSchemeModel.update(cPs.id.get, cPs)
        updatePs must beSome
        updatePs.get.name mustEqual "Test Permission Scheme 1!"

        // Nix it
        PermissionSchemeModel.delete(newPs.id.get)
        val gone =  PermissionSchemeModel.getById(newPs.id.get)
        gone must beNone
      }
    }

    "talk permissions" in {
      running(FakeApplication()) {

        val perms = PermissionSchemeModel.getAllPermissions
        perms.isEmpty must beFalse
      }
    }

    "allow adding users" in {
      running(FakeApplication()) {

        val work = WorkflowModel.getById(1) // Assumes the default workflow exists

        val iu = models.InitialUser(
          username = "testuser1",
          password = "1234",
          realName = "Test User",
          email    = "test@example.com",
          dateCreated = new Date
        )
        val newUser = UserModel.create(iu)

        val psObj = models.PermissionScheme(
          name = "Test Permission Schema",
          description = Some("Testing!"),
          dateCreated = new Date
        )
        val newPs = PermissionSchemeModel.create(psObj)

        val p = models.Project(
          name = "Test Project 1",
          key = "TEST1",
          workflowId = work.get.id.get,
          ownerId = None,
          permissionSchemeId = newPs.id.get,
          defaultPriorityId = None,
          defaultSeverityId = None,
          defaultTypeId = None,
          defaultAssignee = None,
          dateCreated = new Date
        )
        val newProject = ProjectModel.create(p)

        val perms = PermissionSchemeModel.getAllPermissions

        // Start with no permission
        val cant = PermissionSchemeModel.hasPermission(
          projectId = newProject.id.get,
          perm = perms.head.name,
          userId = newUser.id.get
        )
        cant must beNone

        // Now add the permission
        PermissionSchemeModel.addUserToScheme(
          permissionSchemeId = newPs.id.get,
          perm = perms.head.name,
          userId = newUser.id.get
        )

        val can = PermissionSchemeModel.hasPermission(
          projectId = newProject.id.get,
          perm = perms.head.name,
          userId = newUser.id.get
        )
        can must beSome

        ProjectModel.delete(newProject.id.get)
        PermissionSchemeModel.removeUserFromScheme(
          permissionSchemeId = newPs.id.get,
          perm = perms.head.name,
          userId = newUser.id.get
        )
        PermissionSchemeModel.delete(newPs.id.get)
        UserModel.delete(newUser.id.get)
      }
    }

    "allow adding groups" in {
      running(FakeApplication()) {

        val work = WorkflowModel.getById(1) // Assumes the default workflow exists

        val group = models.Group(id = NotAssigned, name = "Test Group!", dateCreated = new Date())
        val newGroup = GroupModel.create(group)

        val iu = models.InitialUser(
          username = "testuser1",
          password = "1234",
          realName = "Test User",
          email    = "test@example.com",
          dateCreated = new Date
        )
        val newUser = UserModel.create(iu)

        // Add user to the group
        GroupModel.addUser(userId = newUser.id.get, groupId = newGroup.id.get)

        val psObj = models.PermissionScheme(
          name = "Test Permission Schema",
          description = Some("Testing!"),
          dateCreated = new Date
        )
        val newPs = PermissionSchemeModel.create(psObj)

        val p = models.Project(
          name = "Test Project 1",
          key = "TEST1",
          workflowId = work.get.id.get,
          ownerId = None,
          permissionSchemeId = newPs.id.get,
          defaultPriorityId = None,
          defaultSeverityId = None,
          defaultTypeId = None,
          defaultAssignee = None,
          dateCreated = new Date
        )
        val newProject = ProjectModel.create(p)

        val perms = PermissionSchemeModel.getAllPermissions

        // Start with no permission
        val cant = PermissionSchemeModel.hasPermission(
          projectId = newProject.id.get,
          perm = perms.head.name,
          userId = newUser.id.get
        )
        cant must beNone

        // Now add the permission
        PermissionSchemeModel.addGroupToScheme(
          permissionSchemeId = newPs.id.get,
          perm = perms.head.name,
          groupId = newGroup.id.get
        )

        val can = PermissionSchemeModel.hasPermission(
          projectId = newProject.id.get,
          perm = perms.head.name,
          userId = newUser.id.get
        )
        can must beSome

        ProjectModel.delete(newProject.id.get)
        PermissionSchemeModel.removeGroupFromScheme(
          permissionSchemeId = newPs.id.get,
          perm = perms.head.name,
          groupId = newGroup.id.get
        )

        PermissionSchemeModel.delete(newPs.id.get)

        GroupModel.removeUser(userId = newUser.id.get, groupId = newGroup.id.get)
        GroupModel.delete(newGroup.id.get)
        UserModel.delete(newUser.id.get)
      }
    }
  }
}
