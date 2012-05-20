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
import models.TicketSeverityModel

object TicketSeverity extends Controller with Secured {

  val severityForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "position" -> number
    )(models.TicketSeverity.apply)(models.TicketSeverity.unapply)
  )

  def add = IsAuthenticated { implicit request =>

    severityForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.severity.create(errors)),
      {
        case severity: models.TicketSeverity =>
        TicketSeverityModel.create(severity)
        Redirect("/admin/ticket/severity") // XXX
      }
    )
  }
  
  def create = IsAuthenticated { implicit request =>

    Ok(views.html.admin.ticket.severity.create(severityForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated { implicit request =>

    val severities = TicketSeverityModel.list(page = page, count = count)

    Ok(views.html.admin.ticket.severity.index(severities)(request))
  }

  def edit(severityId: Long) = IsAuthenticated { implicit request =>

    val severity = TicketSeverityModel.findById(severityId)

    severity match {
      case Some(value) => Ok(views.html.admin.ticket.severity.edit(severityId, severityForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(severityId: Long) = IsAuthenticated { implicit request =>
    
    val severity = TicketSeverityModel.findById(severityId)

    severity match {
      case Some(value) => Ok(views.html.admin.ticket.severity.item(value)(request))
      case None => NotFound
    }
    
  }
  
  def update(severityId: Long) = IsAuthenticated { implicit request =>

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