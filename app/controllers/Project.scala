package controllers

import anorm._
import emp._
import emp.util.Search._
import java.util.Date
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc._
import play.api.libs.json.Json
import models._
import royal.ends.Search._

object Project extends Controller with Secured {

  val addProjectForm = Form(
    mapping(
      "id"  -> ignored(NotAssigned:Pk[Long]),
      "workflow_id" -> longNumber,
      "sequence_current" -> ignored(0.toLong),
      "name"-> nonEmptyText,
      "key" -> text(minLength = 3, maxLength = 16),
      "owner_id" -> optional(longNumber),
      "permission_scheme_id" -> longNumber,
      "default_priority_id" -> optional(longNumber),
      "default_severity_id" -> optional(longNumber),
      "default_type_id" -> optional(longNumber),
      "default_assignee" -> optional(number),
      "date_created" -> ignored(new Date())
    )(models.Project.apply)(models.Project.unapply)
  )

  val editProjectForm = Form(
    mapping(
      "id"  -> ignored(NotAssigned:Pk[Long]),
      "workflow_id" -> longNumber,
      "name"-> nonEmptyText,
      "owner_id" -> optional(longNumber),
      "permission_scheme_id" -> longNumber,
      "default_priority_id" -> optional(longNumber),
      "default_severity_id" -> optional(longNumber),
      "default_type_id" -> optional(longNumber),
      "default_assignee" -> optional(number)
    )(models.EditProject.apply)(models.EditProject.unapply)
  )

  def add = IsAuthenticated(perm = "PERM_GLOBAL_PROJECT_CREATE") { implicit request =>

    addProjectForm.bindFromRequest.fold(
      errors => {
        val workflows = WorkflowModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

        val users = UserModel.getAll.map { x => (x.id.get.toString -> x.realName) }
        val asses = DefaultAssignee.values.map { x => (x.id.toString -> Messages(x.toString)) }
        val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

        BadRequest(views.html.project.create(errors, workflows, users, asses.toList, ttypes, prios, sevs))
      },
      value => {
        val project = ProjectModel.create(value)
        Redirect(routes.Project.item(project.id.get)).flashing("success" -> "project.add.success")
      }
    )
  }

  def create = IsAuthenticated(perm = "PERM_GLOBAL_PROJECT_CREATE") { implicit request =>

    val workflows = WorkflowModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

    val users = UserModel.getAll.map { x => (x.id.get.toString -> x.realName) }
    val asses = DefaultAssignee.values.map { x => (x.id.toString -> Messages(x.toString)) }
    val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

    Ok(views.html.project.create(addProjectForm, workflows, users, asses.toList, ttypes, prios, sevs)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated() { implicit request =>

    val projs = ProjectModel.list(userId = request.session.get("user_id").get.toLong, page = page, count = count)

    Ok(views.html.project.index(projs)(request))
  }

  def edit(projectId: Long) = IsAuthenticated(projectId = Some(projectId), perm = "PERM_PROJECT_ADMIN") { implicit request =>

    val project = ProjectModel.getById(projectId)
    val workflows = WorkflowModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

    val users = UserModel.getAll.map { x => (x.id.get.toString -> x.realName) }
    val asses = DefaultAssignee.values.map { x => (x.id.toString -> Messages(x.toString)) }
    val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val perms = PermissionSchemeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

    project match {
      case Some(value) => {
        val editProject = EditProject(
          id = value.id,
          workflowId = value.workflowId,
          name = value.name,
          ownerId = value.ownerId,
          permissionSchemeId = value.permissionSchemeId,
          defaultPriorityId = value.defaultPriorityId,
          defaultSeverityId = value.defaultSeverityId,
          defaultTypeId = value.defaultTypeId,
          defaultAssignee = value.defaultAssignee
        )
        Ok(views.html.project.edit(projectId, editProjectForm.fill(editProject), workflows, users, asses.toList, ttypes, prios, sevs, perms)(request))
      }
      case None => NotFound
    }
  }

  def item(projectId: Long) = IsAuthenticated(projectId = Some(projectId), perm = "PERM_PROJECT_BROWSE") { implicit request =>

    val project = ProjectModel.getById(projectId)

    val efilters = Map("project_id" -> Seq(projectId.toString))

    val userId = request.session.get("user_id").get.toLong

    val eventQuery = SearchQuery(userId = userId, filters = efilters)

    val events = SearchModel.searchEvent(eventQuery) // XXX fixed page, count, query

    val tfilters = Map(
      "project_id"  -> Seq(projectId.toString),
      "resolution"  -> Seq("TICK_RESO_UNRESOLVED")
    )

    val ticketQuery = SearchQuery(userId = userId, filters = tfilters)

    val tickets = SearchModel.searchTicket(ticketQuery) // XXX Fixed page, count, query

    project match {
      case Some(value) => Ok(views.html.project.item(value, tickets, events)(request))
      case None => NotFound
    }
  }

  def list(page: Int, count: Int) = IsAuthenticated() { implicit request =>

    val objs = ProjectModel.list(userId = request.session.get("user_id").get.toLong, page = page, count = count)

    Ok(views.html.project.list(objs)(request))
  }

  def update(projectId: Long) = IsAuthenticated(projectId = Some(projectId), perm = "PERM_PROJECT_ADMIN") { implicit request =>

    editProjectForm.bindFromRequest.fold(
      errors => {
        val workflows = WorkflowModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val users = UserModel.getAll.map { x => (x.id.get.toString -> x.realName) }
        val asses = DefaultAssignee.values.map { x => (x.id.toString -> Messages(x.toString)) }
        val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val perms = PermissionSchemeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

        BadRequest(views.html.project.edit(projectId, errors, workflows, users, asses.toList, ttypes, prios, sevs, perms))
      },
      value => {
        ProjectModel.update(projectId, value)
        Redirect(routes.Project.item(projectId)).flashing("success" -> "project.edit.success")
      }
    )
  }
}
