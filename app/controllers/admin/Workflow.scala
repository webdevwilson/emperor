package controllers.admin

import anorm._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._
import chc._
import models.WorkflowModel

object Workflow extends Controller {

  val objForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "description" -> optional(text)
    )(models.Workflow.apply)(models.Workflow.unapply)
  )

  def add = Action { implicit request =>

    objForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.workflow.create(errors)),
      {
        case workflow: models.Workflow =>
          WorkflowModel.create(workflow)
          Redirect("/admin/workflow") // XXX
      }
    )
  }
  
  def create = Action { implicit request =>

    Ok(views.html.admin.workflow.create(objForm)(request))
  }

  def index(page: Int, count: Int) = Action { implicit request =>

    val workflows = WorkflowModel.list(page = page, count = count)

    Ok(views.html.admin.workflow.index(workflows)(request))
  }

  def edit(workflowId: Long) = Action { implicit request =>

    val workflow = WorkflowModel.findById(workflowId)

    workflow match {
      case Some(value) => Ok(views.html.admin.workflow.edit(workflowId, objForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(workflowId: Long) = Action { implicit request =>
    
    val workflow = WorkflowModel.findById(workflowId)
    val statuses = WorkflowModel.findStatuses(workflowId)

    workflow match {
      case Some(value) => Ok(views.html.admin.workflow.item(value, statuses)(request))
      case None => NotFound
    }
    
  }
  
  def update(workflowId: Long) = Action { implicit request =>

    objForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.workflow.edit(workflowId, errors)),
      {
        case workflow: models.Workflow =>
        WorkflowModel.update(workflowId, workflow)
        Redirect("/admin/workflow") // XXX
      }
    )
  }
}