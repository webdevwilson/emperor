package test

import anorm._
import anorm.NotAssigned
import java.util.Date
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class ProjectModelSpec extends Specification {

  import models.{ProjectModel,WorkflowModel}

  "Project model" should {

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
        newProject must beAnInstanceOf[models.Project]

        // Get it by id
        val proj = ProjectModel.getById(newProject.id.get)
        proj must beSome
        proj.get must beAnInstanceOf[models.Project]

        // // Change it
        val cProj = proj.get.copy(name = "Test Project 1!")
        val updateProj = ProjectModel.update(cProj.id.get, cProj)
        updateProj must beSome
        updateProj.get.name mustEqual "Test Project 1!"

        // Nix it
        ProjectModel.delete(newProject.id.get)
        val gone =  ProjectModel.getById(newProject.id.get)
        gone must beNone
      }
    }

    "handle sequences" in {
      running(FakeApplication()) {

        val work = WorkflowModel.getById(1) // Assumes the default workflow exists

        val p = models.Project(
          name = "Test Project 1",
          key = "TEST1",
          workflowId = work.get.id.get,
          dateCreated = new Date
        )
        val newProject = ProjectModel.create(p)
        newProject.sequenceCurrent mustEqual(0)

        // Verify it increments
        val neyext = ProjectModel.getNextSequence(newProject.id.get)
        neyext must beSome
        neyext.get mustEqual(1)

        ProjectModel.delete(newProject.id.get)
        1 mustEqual(1)
      }
    }
  }
}
