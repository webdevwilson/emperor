package controllers.admin

import anorm._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._
import chc._
import models.TicketSeverityModel

object TicketSeverity extends Controller {

  val severityForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "position" -> number
    )(models.TicketSeverity.apply)(models.TicketSeverity.unapply)
  )

  def add = Action { implicit request =>

    severityForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.severity.create(errors)),
      {
        case severity: models.TicketSeverity =>
        TicketSeverityModel.create(severity)
        Redirect("/admin/ticket/severity") // XXX
      }
    )
  }
  
  def create = Action { implicit request =>

    Ok(views.html.admin.ticket.severity.create(severityForm)(request))
  }

  def index(page: Int, count: Int) = Action { implicit request =>

    val severities = TicketSeverityModel.list(page = page, count = count)

    Ok(views.html.admin.ticket.severity.index(severities)(request))
  }

  def edit(severityId: Long) = Action { implicit request =>

    val severity = TicketSeverityModel.findById(severityId)

    severity match {
      case Some(value) => Ok(views.html.admin.ticket.severity.edit(severityId, severityForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(severityId: Long) = Action { implicit request =>
    
    val severity = TicketSeverityModel.findById(severityId)

    severity match {
      case Some(value) => Ok(views.html.admin.ticket.severity.item(value)(request))
      case None => NotFound
    }
    
  }
  
  def update(severityId: Long) = Action { implicit request =>

    severityForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.severity.edit(severityId, errors)),
      {
        case severity: models.TicketSeverity =>
        TicketSeverityModel.update(severityId, severity)
        Redirect("/admin/ticket/severity") // XXX
      }
    )
  }
}