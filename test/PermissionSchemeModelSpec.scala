package test

import anorm._
import anorm.NotAssigned
import java.util.Date
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class PermissionSchemeModelSpec extends Specification {

  import models.PermissionSchemeModel

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
  }
}
