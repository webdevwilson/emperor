package controllers.admin

import anorm._
import emp._
import controllers._
import org.joda.time.DateTime
import models.TicketStatusModel
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.api.db._

object TicketStatus extends Controller with Secured {

  val statusForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "date_created" -> ignored(new DateTime())
    )(models.TicketStatus.apply)(models.TicketStatus.unapply)
  )

  def add = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    statusForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.status.create(errors)),
      value => {
        val status = TicketStatusModel.create(value)
        Redirect(routes.TicketStatus.item(status.id.get)).flashing("success" -> "admin.ticket_status.add.success")
      }
    )
  }

  def create = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    Ok(views.html.admin.ticket.status.create(statusForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val statuses = TicketStatusModel.list(page = page, count = count)

    Ok(views.html.admin.ticket.status.index(statuses)(request))
  }

  def edit(statusId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val status = TicketStatusModel.getById(statusId)

    status match {
      case Some(value) => Ok(views.html.admin.ticket.status.edit(statusId, statusForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(statusId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val status = TicketStatusModel.getById(statusId)

    status match {
      case Some(value) => Ok(views.html.admin.ticket.status.item(value)(request))
      case None => NotFound
    }

  }

  def update(statusId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    statusForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.status.edit(statusId, errors)),
      {
        case status: models.TicketStatus =>
        TicketStatusModel.update(statusId, status)
        Redirect(routes.TicketStatus.item(statusId)).flashing("success" -> "admin.ticket_status.edit.success")
      }
    )
  }
}