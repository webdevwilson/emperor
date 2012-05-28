package controllers.admin

import anorm._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._
import chc._
import controllers._
import java.util.Date
import models.TicketStatusModel
import models.WorkflowModel

object Workflow extends Controller with Secured {

  val objForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> optional(text)
    )(models.InitialWorkflow.apply)(models.InitialWorkflow.unapply)
  )

  val editForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "description" -> optional(text),
      "date_created" -> ignored(new Date()) // A little white lie, ignored anyway
    )(models.Workflow.apply)(models.Workflow.unapply)
  )

  def add = IsAuthenticated { implicit request =>

    objForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.workflow.create(errors)),
      {
        case workflow: models.Workflow =>
          WorkflowModel.create(workflow)
          Redirect("/admin/workflow") // XXX
      }
    )
  }
  
  def create = IsAuthenticated { implicit request =>

    Ok(views.html.admin.workflow.create(objForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated { implicit request =>

    val workflows = WorkflowModel.list(page = page, count = count)

    Ok(views.html.admin.workflow.index(workflows)(request))
  }

  def edit(workflowId: Long) = IsAuthenticated { implicit request =>

    val workflow = WorkflowModel.findById(workflowId)

    workflow match {
      case Some(value) => Ok(views.html.admin.workflow.edit(workflowId, editForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(workflowId: Long) = IsAuthenticated { implicit request =>
    
    val workflow = WorkflowModel.findById(workflowId)
    val statuses = WorkflowModel.findStatuses(workflowId)

    workflow match {
      case Some(value) => Ok(views.html.admin.workflow.item(value, statuses)(request))
      case None => NotFound
    }
  }
  
  def modify(workflowId: Long) = IsAuthenticated { implicit request =>
    
    val workflow = WorkflowModel.findById(workflowId)
    val statuses = WorkflowModel.findStatuses(workflowId)
    val unused = TicketStatusModel.getAll filterNot { status => statuses exists { ws => ws.statusId == status.id.get } }

    workflow match {
      case Some(value) => Ok(views.html.admin.workflow.modify(workflowId, value, statuses, unused)(request))
      case None => NotFound
    }
  }
  
  def update(workflowId: Long) = IsAuthenticated { implicit request =>

    editForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.workflow.edit(workflowId, errors)),
      {
        case workflow: models.Workflow =>
          WorkflowModel.update(workflowId, workflow)
          Redirect("/admin/workflow") // XXX
      }
    )
  }

  def save(workflowId: Long) = IsAuthenticated { implicit request =>

    Redirect("/admin/workflow") // XXX
  }
}