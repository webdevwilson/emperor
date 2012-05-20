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
import models.TicketPriorityModel

object TicketPriority extends Controller with Secured {

  val priorityForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "position" -> number
    )(models.TicketPriority.apply)(models.TicketPriority.unapply)
  )

  def add = IsAuthenticated { implicit request =>

    priorityForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.priority.create(errors)),
      {
        case priority: models.TicketPriority =>
        TicketPriorityModel.create(priority)
        Redirect("/admin/ticket/priority") // XXX
      }
    )
  }
  
  def create = IsAuthenticated { implicit request =>

    Ok(views.html.admin.ticket.priority.create(priorityForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated { implicit request =>

    val priorities = TicketPriorityModel.list(page = page, count = count)

    Ok(views.html.admin.ticket.priority.index(priorities)(request))
  }

  def edit(priorityId: Long) = IsAuthenticated { implicit request =>

    val priority = TicketPriorityModel.findById(priorityId)

    priority match {
      case Some(value) => Ok(views.html.admin.ticket.priority.edit(priorityId, priorityForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(priorityId: Long) = IsAuthenticated { implicit request =>
    
    val priority = TicketPriorityModel.findById(priorityId)

    priority match {
      case Some(value) => Ok(views.html.admin.ticket.priority.item(value)(request))
      case None => NotFound
    }
    
  }
  
  def update(priorityId: Long) = IsAuthenticated { implicit request =>

    priorityForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.priority.edit(priorityId, errors)),
      {
        case priority: models.TicketPriority =>
        TicketPriorityModel.update(priorityId, priority)
        Redirect("/admin/ticket/priority") // XXX
      }
    )
  }
}