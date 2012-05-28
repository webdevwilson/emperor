package controllers

import anorm._
import chc._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc._
import play.api.libs.json.Json
import play.api.libs.json.Json._
import models.ProjectModel
import models._
import org.clapper.markwrap._

object Ticket extends Controller with Secured {

  val mdParser = MarkWrap.parserFor(MarkupType.Markdown)

  val statusChangeForm = Form(
    mapping(
      "status_id" -> longNumber,
      "comment"   -> optional(text)
    )(models.StatusChange.apply)(models.StatusChange.unapply)
  )

  val commentForm = Form(
    mapping(
      "content" -> nonEmptyText
    )(models.InitialComment.apply)(models.InitialComment.unapply)
  )

  val initialTicketForm = Form(
    mapping(
      "user_id"     -> longNumber,
      "project_id"  -> longNumber,
      "priority_id" -> longNumber,
      "severity_id" -> longNumber,
      "type_id"     -> longNumber,
      "position"    -> optional(longNumber),
      "summary"     -> nonEmptyText,
      "description" -> optional(text)
    )(models.InitialTicket.apply)(models.InitialTicket.unapply)
  )

  val ticketForm = Form(
    mapping(
      "id"            -> ignored(NotAssigned:Pk[Long]),
      "reporter_id"   -> longNumber,
      "project_id"    -> longNumber,
      "priority_id"   -> longNumber,
      "resolution_id" -> optional(longNumber),
      "proposed_resolution_id" -> optional(longNumber),
      "severity_id"   -> longNumber,
      "type_id"       -> longNumber,
      "position"      -> optional(longNumber),
      "summary"       -> nonEmptyText,
      "description"   -> optional(text)
    )(models.EditTicket.apply)(models.EditTicket.unapply)
  )

  def newStatus(ticketId: Long, statusId: Long) = IsAuthenticated { implicit request =>
    
    val ticket = TicketModel.getFullById(ticketId)
    val newStatus = WorkflowModel.getStatusById(statusId)
    // XXX some sort of check too many gets!
    
    ticket match {
      case Some(value) => {
        newStatus match {
          case Some(status) => Ok(views.html.ticket.newstatus(ticketId, value, status, commentForm)(request))
          case None => BadRequest(views.html.ticket.error(request))
        }
      }
      case None => BadRequest(views.html.ticket.error(request))
    }

  }

  def status(ticketId: Long) = IsAuthenticated { implicit request =>

    val ticket = TicketModel.getFullById(ticketId)
    val comments = TicketModel.getComments(ticketId)

    ticket match {
      case Some(value) => {
        statusChangeForm.bindFromRequest.fold(
          errors => {
            val prevStatus = WorkflowModel.getPreviousStatus(value.workflowStatusId)
            val nextStatus = WorkflowModel.getNextStatus(value.workflowStatusId)
            Redirect(routes.Ticket.item(ticketId)).flashing("error" -> "ticket.error.status")
          }, {
            case statusChange: models.StatusChange =>
              TicketModel.advance(ticketId, statusChange.statusId)
              Redirect(routes.Ticket.item(ticketId)).flashing("success" -> "ticket.success.status")
          }
        )
      }
      case None => NotFound
    }
  }

  def add = IsAuthenticated { implicit request =>

    initialTicketForm.bindFromRequest.fold(
      errors => {
        // Should be i18ned in the view
        val users = UserModel.getAll.map { x => (x.id.get.toString -> x.realName) }
        val projs = ProjectModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

        BadRequest(views.html.ticket.create(errors, users, projs, ttypes, prios, sevs))
      },
      value => {
        val ticket = TicketModel.create(value)
        Redirect(routes.Ticket.item(ticket.get.id.get)).flashing("success" -> "ticket.add.success")
      }
    )
  }

  def comment(ticketId: Long) = IsAuthenticated { implicit request =>

    commentForm.bindFromRequest.fold(
      errors => {
        Redirect(routes.Ticket.item(ticketId)).flashing("error" -> "ticket.comment.invalid")
      },
      value => {
        TicketModel.addComment(ticketId, request.session.get("userId").get.toLong, value.content)
        Redirect(routes.Ticket.item(ticketId)).flashing("success" -> "ticket.comment.added")
      }
    )
  }
  
  def create = IsAuthenticated { implicit request =>

    // Should be i18ned in the view
    val users = UserModel.getAll.map { x => (x.id.get.toString -> x.realName) }
    val projs = ProjectModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

    Ok(views.html.ticket.create(initialTicketForm, users, projs, ttypes, prios, sevs)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated { implicit request =>

    val groups = TicketModel.list(page = page, count = count)

    Ok(views.html.ticket.index(groups)(request))
  }

  def edit(ticketId: Long) = IsAuthenticated { implicit request =>

    val users = UserModel.getAll.map { x => (x.id.get.toString -> x.realName) }
    val ticket = TicketModel.getById(ticketId)
    // XXX Should really match this here and return if it's not found
    val projs = ProjectModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

    ticket match {
      case Some(value) => Ok(views.html.ticket.edit(ticketId, ticketForm.fill(value), users, projs, ttypes, prios, sevs)(request))
      case None => NotFound
    }
  }

  def item(ticketId: Long) = IsAuthenticated { implicit request =>
    
    val ticket = TicketModel.getFullById(ticketId)
    val history = TicketModel.getHistory(ticketId)
    val changes = TicketModel.getChanges(ticketId, history.items)

    ticket match {
      case Some(value) => {

        val prevStatus = WorkflowModel.getPreviousStatus(value.workflowStatusId)
        val nextStatus = WorkflowModel.getNextStatus(value.workflowStatusId)
        val searchComments = TicketModel.getCommentsAsSearchResult(ticketId = ticketId)
        Ok(views.html.ticket.item(value, mdParser, commentForm, prevStatus, nextStatus, searchComments, changes)(request))
      }
      case None => NotFound
    }
    
  }
  
  def update(ticketId: Long) = IsAuthenticated { implicit request =>

    ticketForm.bindFromRequest.fold(
      errors => {
        val users = UserModel.getAll.map { x => (x.id.get.toString -> x.realName) }
        val projs = ProjectModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

        BadRequest(views.html.ticket.edit(ticketId, errors, users, projs, ttypes, prios, sevs))
      },
      value => {
        TicketModel.update(request.session.get("userId").get.toLong, ticketId, value)
        Redirect(routes.Ticket.item(ticketId)).flashing("success" -> "ticket.edit.success")
      }
    )
  }
}