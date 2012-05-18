package controllers

import anorm._
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

object Ticket extends Controller {

  val mdParser = MarkWrap.parserFor(MarkupType.Markdown)

  val statusChangeForm = Form(
    mapping(
      "status_id" -> longNumber,
      "comment"   -> optional(text)
    )(models.StatusChange.apply)(models.StatusChange.unapply)
  )

  val commentForm = Form(
    mapping(
      "contents" -> nonEmptyText
    )(models.InitialComment.apply)(models.InitialComment.unapply)
  )

  val initialTicketForm = Form(
    mapping(
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
      "project_id"    -> longNumber,
      "priority_id"   -> longNumber,
      "resolution_id" -> optional(longNumber),
      "proposed_resolution_id" -> optional(longNumber),
      "severity_id"   -> longNumber,
      "status_id"     -> longNumber,
      "type_id"       -> longNumber,
      "position"      -> optional(longNumber),
      "summary"       -> nonEmptyText,
      "description"   -> optional(text)
    )(models.Ticket.apply)(models.Ticket.unapply)
  )

  def advance(ticketId: Long) = Action { implicit request =>

    statusChangeForm.bindFromRequest.fold(
      errors => {
        val prevStatus = WorkflowModel.getPreviousStatus(value.workflowStatusId)
        val nextStatus = WorkflowModel.getNextStatus(value.workflowStatusId)    
        BadRequest(views.html.ticket.item(value, mdParser, commentForm, prevStatus, nextStatus)(request))
      }, {
        case statusChange: models.StatusChange =>
          TicketModel.advance(ticketId, statusChange.statusId)
          Redirect("/ticket") // XXX
      }
    )
  }

  def add = Action { implicit request =>

    initialTicketForm.bindFromRequest.fold(
      errors => {
        val projs = ProjectModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

        BadRequest(views.html.ticket.create(errors, projs, ttypes, prios, sevs))
      },{
        case ticket: models.InitialTicket =>
          TicketModel.create(ticket)
          Redirect("/ticket") // XXX
      }
    )
  }

  def comment(ticketId: Long) = Action { implicit request =>

    commentForm.bindFromRequest.fold(
      errors => BadRequest("Missing parameter 'content'"),
      values => Ok(toJson(Map("status" -> "OK")))
    )
  }
  
  def create = Action { implicit request =>

    val projs = ProjectModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

    Ok(views.html.ticket.create(initialTicketForm, projs, ttypes, prios, sevs)(request))
  }

  def index(page: Int, count: Int) = Action { implicit request =>

    val groups = TicketModel.list(page = page, count = count)

    Ok(views.html.ticket.index(groups)(request))
  }

  def edit(ticketId: Long) = Action { implicit request =>

    val ticket = TicketModel.findById(ticketId)
    // XXX Should really match this here and return if it's not found
    val projs = ProjectModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

    ticket match {
      case Some(value) => Ok(views.html.ticket.edit(ticketId, ticketForm.fill(value), projs, ttypes, prios, sevs)(request))
      case None => NotFound
    }
  }

  def item(ticketId: Long) = Action { implicit request =>
    
    val ticket = TicketModel.findFullById(ticketId)

    ticket match {
      case Some(value) => {

        val prevStatus = WorkflowModel.getPreviousStatus(value.workflowStatusId)
        val nextStatus = WorkflowModel.getNextStatus(value.workflowStatusId)    
        Ok(views.html.ticket.item(value, mdParser, commentForm, prevStatus, nextStatus)(request))
      }
      case None => NotFound
    }
    
  }
  
  def update(ticketId: Long) = Action { implicit request =>

    ticketForm.bindFromRequest.fold(
      errors => {
        val projs = ProjectModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

        BadRequest(views.html.ticket.edit(ticketId, errors, projs, ttypes, prios, sevs))
      }, {
        case ticket: models.Ticket =>
        TicketModel.update(ticketId, ticket)
        Redirect("/admin") // XXX
      }
    )
  }
}