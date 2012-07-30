package controllers

import anorm._
import chc._
import java.util.Date
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc._
import play.api.libs.json.Json
import models.{ProjectModel,SearchModel,WorkflowModel}

object Project extends Controller with Secured {

  val projectForm = Form(
    mapping(
      "id"  -> ignored(NotAssigned:Pk[Long]),
      "workflow_id" -> longNumber,
      "sequence_current" -> ignored(0.toLong),
      "name"-> nonEmptyText,
      "key" -> text(minLength = 3, maxLength = 16),
      "date_created" -> ignored(new Date())
    )(models.Project.apply)(models.Project.unapply)
  )

  def add = IsAuthenticated { implicit request =>

    projectForm.bindFromRequest.fold(
      errors => {
        val workflows = WorkflowModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        BadRequest(views.html.project.create(errors, workflows))
      },
      value => {
        val project = ProjectModel.create(value)
        Redirect(routes.Project.item(project.id.get)).flashing("success" -> "project.add.success")
      }
    )
  }

  def create = IsAuthenticated { implicit request =>

    val workflows = WorkflowModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

    Ok(views.html.project.create(projectForm, workflows)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated { implicit request =>

    val projs = ProjectModel.list(page = page, count = count)

    Ok(views.html.project.index(projs)(request))
  }

  def edit(projectId: Long) = IsAuthenticated { implicit request =>

    val project = ProjectModel.getById(projectId)
    val workflows = WorkflowModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

    project match {
      case Some(value) => Ok(views.html.project.edit(projectId, projectForm.fill(value), workflows)(request))
      case None => NotFound
    }
  }

  def item(projectId: Long) = IsAuthenticated { implicit request =>

    val project = ProjectModel.getById(projectId)

    val filters = Map("project_id" -> Seq(projectId.toString))

    val response = SearchModel.searchEvent(1, 10, "", filters) // XX fixed page, count, query

    project match {
      case Some(value) => Ok(views.html.project.item(value, response)(request))
      case None => NotFound
    }
  }

  def list(page: Int, count: Int) = IsAuthenticated { implicit request =>

    val groups = ProjectModel.list(page = page, count = count)

    Ok(views.html.project.list(groups)(request))
  }

  def update(projectId: Long) = IsAuthenticated { implicit request =>

    projectForm.bindFromRequest.fold(
      errors => {
        val workflows = WorkflowModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        BadRequest(views.html.project.edit(projectId, errors, workflows))
      },
      value => {
        ProjectModel.update(projectId, value)
        Redirect(routes.Project.item(projectId)).flashing("success" -> "project.edit.success")
      }
    )
  }
}
