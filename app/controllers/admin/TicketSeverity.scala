package controllers.admin

import anorm._
import emp._
import controllers._
import org.joda.time.DateTime
import models.TicketSeverityModel
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._

object TicketSeverity extends Controller with Secured {

  val severityForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "color" -> nonEmptyText,
      "position" -> number,
      "date_created" -> ignored(new DateTime())
    )(models.TicketSeverity.apply)(models.TicketSeverity.unapply)
  )

  def add = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    severityForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.severity.create(errors)),
      value => {
        val sev = TicketSeverityModel.create(value)
        Redirect(routes.TicketSeverity.item(sev.id.get)).flashing("success" -> "admin.ticket_severity.add.success")
      }
    )
  }

  def create = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    Ok(views.html.admin.ticket.severity.create(severityForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val severities = TicketSeverityModel.list(page = page, count = count)

    Ok(views.html.admin.ticket.severity.index(severities)(request))
  }

  def edit(severityId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val severity = TicketSeverityModel.getById(severityId)

    severity match {
      case Some(value) => Ok(views.html.admin.ticket.severity.edit(severityId, severityForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(severityId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val severity = TicketSeverityModel.getById(severityId)

    severity match {
      case Some(value) => Ok(views.html.admin.ticket.severity.item(value)(request))
      case None => NotFound
    }

  }

  def update(severityId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    severityForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.severity.edit(severityId, errors)),
      value => {
        TicketSeverityModel.update(severityId, value)
        Redirect(routes.TicketSeverity.item(severityId)).flashing("success" -> "admin.ticket_severity.edit.success")
      }
    )
  }
}