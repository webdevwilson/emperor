package controllers.admin

import anorm._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._
import chc._
import models.TicketStatusModel

object TicketStatus extends Controller {

  val statusForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText
    )(models.TicketStatus.apply)(models.TicketStatus.unapply)
  )

  def add = Action { implicit request =>

    statusForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.status.create(errors)),
      {
        case status: models.TicketStatus =>
        TicketStatusModel.create(status)
        Redirect("/admin/ticket/status") // XXX
      }
    )
  }
  
  def create = Action { implicit request =>

    Ok(views.html.admin.ticket.status.create(statusForm)(request))
  }

  def index(page: Int, count: Int) = Action { implicit request =>

    val statuses = TicketStatusModel.list(page = page, count = count)

    Ok(views.html.admin.ticket.status.index(statuses)(request))
  }

  def edit(statusId: Long) = Action { implicit request =>

    val status = TicketStatusModel.findById(statusId)

    status match {
      case Some(value) => Ok(views.html.admin.ticket.status.edit(statusId, statusForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(statusId: Long) = Action { implicit request =>
    
    val status = TicketStatusModel.findById(statusId)

    status match {
      case Some(value) => Ok(views.html.admin.ticket.status.item(value)(request))
      case None => NotFound
    }
    
  }
  
  def update(statusId: Long) = Action { implicit request =>

    statusForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.status.edit(statusId, errors)),
      {
        case status: models.TicketSeverity =>
        TicketStatusModel.update(statusId, status)
        Redirect("/admin/ticket/status") // XXX
      }
    )
  }
}