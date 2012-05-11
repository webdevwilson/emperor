package controllers

import anorm._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc._
import play.api.libs.json.Json
import models.TicketModel
import models.TicketPriorityModel
import models.TicketSeverityModel
import models.TicketTypeModel

object Ticket extends Controller {

  val ticketForm = Form(
    mapping(
      "priority_id" -> longNumber,
      "severity_id" -> longNumber,
      "type_id" -> longNumber,
      "position" -> optional(longNumber),
      "summary" -> nonEmptyText,
      "description" -> optional(text)
    )(models.InitialTicket.apply)(models.InitialTicket.unapply)
  )

  def add = Action { implicit request =>

    val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

    ticketForm.bindFromRequest.fold(
      errors => BadRequest(views.html.ticket.create(errors, ttypes, prios, sevs)),
      {
        case ticket: models.InitialTicket =>
        
        val realTick = models.Ticket(
          id = NotAssigned, priorityId = ticket.priorityId, resolutionId = None,
          statusId = 1.toLong, // XXX This shouldn't be hardcoded
          severityId = ticket.severityId, typeId = ticket.typeId,
          position = ticket.position, summary = ticket.summary,
          description = ticket.description
        )
        
        TicketModel.create(realTick)
        Redirect("/ticket") // XXX
      }
    )
  }
  
  def create = Action { implicit request =>

    val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

    Ok(views.html.ticket.create(ticketForm, ttypes, prios, sevs)(request))
  }

  def index(page: Int, count: Int) = Action { implicit request =>

    val groups = TicketModel.list(page = page, count = count)

    Ok(views.html.ticket.index(groups)(request))
  }

  def edit(ticketId: Long) = Action { implicit request =>

    val ticket = TicketModel.findById(ticketId)

    ticket match {
      case Some(value) => Ok(views.html.ticket.edit(ticketId, ticketForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(ticketId: Long) = Action { implicit request =>
    
    val ticket = TicketModel.findById(ticketId)

    ticket match {
      case Some(value) => Ok(views.html.ticket.item(value)(request))
      case None => NotFound
    }
    
  }
  
  def update(ticketId: Long) = Action { implicit request =>

    ticketForm.bindFromRequest.fold(
      errors => BadRequest(views.html.ticket.edit(ticketId, errors)),
      {
        case ticket: models.Ticket =>
        TicketModel.update(ticketId, ticket)
        Redirect("/admin") // XXX
      }
    )
  }
}