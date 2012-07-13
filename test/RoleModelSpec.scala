package test

import anorm._
import anorm.NotAssigned
import java.util.Date
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class RoleModelSpec extends Specification {

  import models.RoleModel

  "Project model" should {

    "create, retrieve and delete" in {
      running(FakeApplication()) {

        val r = models.Role(
          name = "Test Project 1",
          dateCreated = new Date
        )
        val newRole = RoleModel.create(r)
        newRole must beAnInstanceOf[models.Role]

        // Get it by id
        val role = RoleModel.getById(newRole.id.get)
        role must beSome
        role.get must beAnInstanceOf[models.Role]

        // // Change it
        val cRole = role.get.copy(name = "Test Role 1!")
        val updateRole = RoleModel.update(cRole.id.get, cRole)
        updateRole must beSome
        updateRole.get.name mustEqual "Test Role 1!"

        // Nix it
        RoleModel.delete(newRole.id.get)
        val gone =  RoleModel.getById(newRole.id.get)
        gone must beNone
      }
    }
  }
}
