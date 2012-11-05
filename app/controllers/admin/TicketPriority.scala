package controllers.admin

import anorm._
import emp._
import controllers._
import org.joda.time.DateTime
import models.TicketPriorityModel
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._

object TicketPriority extends Controller with Secured {

  val priorityForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "color" -> nonEmptyText,
      "position" -> number,
      "date_created" -> ignored(new DateTime())
    )(models.TicketPriority.apply)(models.TicketPriority.unapply)
  )

  def add = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    priorityForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.priority.create(errors)),
      value => {
        val priority = TicketPriorityModel.create(value)
        Redirect(routes.TicketPriority.item(priority.id.get)).flashing("success" -> "admin.ticket_priority.add.success")
      }
    )
  }

  def create = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    Ok(views.html.admin.ticket.priority.create(priorityForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val priorities = TicketPriorityModel.list(page = page, count = count)

    Ok(views.html.admin.ticket.priority.index(priorities)(request))
  }

  def edit(priorityId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val priority = TicketPriorityModel.getById(priorityId)

    priority match {
      case Some(value) => Ok(views.html.admin.ticket.priority.edit(priorityId, priorityForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(priorityId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val priority = TicketPriorityModel.getById(priorityId)

    priority match {
      case Some(value) => Ok(views.html.admin.ticket.priority.item(value)(request))
      case None => NotFound
    }
  }

  def update(priorityId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    priorityForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.priority.edit(priorityId, errors)),
      value => {
        TicketPriorityModel.update(priorityId, value)
        Redirect(routes.TicketPriority.item(priorityId)).flashing("success" -> "admin.ticket_priority.edit.success")
      }
    )
  }
}