package controllers.admin

import anorm._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._
import controllers._
import org.joda.time.DateTime
import models.TicketStatusModel
import models.WorkflowModel

object Workflow extends Controller with Secured {

  val objForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "description" -> optional(text),
      "date_created" -> ignored(new DateTime())
    )(models.Workflow.apply)(models.Workflow.unapply)
  )

  def add = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    objForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.workflow.create(errors)),
      value => {
        val workflow = WorkflowModel.create(value)
        Redirect(routes.Workflow.item(workflow.id.get)).flashing("success" -> "admin.workflow.add.success")
      }
    )
  }

  def create = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    Ok(views.html.admin.workflow.create(objForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val workflows = WorkflowModel.list(page = page, count = count)

    Ok(views.html.admin.workflow.index(workflows)(request))
  }

  def edit(workflowId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val workflow = WorkflowModel.getById(workflowId)

    workflow match {
      case Some(value) => Ok(views.html.admin.workflow.edit(workflowId, objForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(workflowId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val workflow = WorkflowModel.getById(workflowId)
    val statuses = WorkflowModel.getStatuses(workflowId)

    workflow match {
      case Some(value) => Ok(views.html.admin.workflow.item(value, statuses)(request))
      case None => NotFound
    }
  }

  def modify(workflowId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val workflow = WorkflowModel.getById(workflowId)
    val statuses = WorkflowModel.getStatuses(workflowId)
    val unused = TicketStatusModel.getAll filterNot { status => statuses exists { ws => ws.statusId == status.id.get } }

    workflow match {
      case Some(value) => Ok(views.html.admin.workflow.modify(workflowId, value, statuses, unused)(request))
      case None => NotFound
    }
  }

  def update(workflowId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    objForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.workflow.edit(workflowId, errors)),
      value => {
        WorkflowModel.update(workflowId, value)
        Redirect(routes.Workflow.item(workflowId)).flashing("success" -> "admin.workflow.edit.success")
      }
    )
  }

  def save(workflowId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    // XXX Do me!
    Redirect(routes.Workflow.item(workflowId)).flashing("success" -> "admin.workflow.edit.success")
  }
}