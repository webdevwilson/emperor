package controllers.admin

import anorm._
import chc._
import controllers._
import java.util.Date
import models.TicketTypeModel
import org.mindrot.jbcrypt.BCrypt
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._

object TicketType extends Controller with Secured {

  val typeForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "color" -> nonEmptyText,
      "date_created" -> ignored(new Date())
    )(models.TicketType.apply)(models.TicketType.unapply)
  )

  def add = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    typeForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.ttype.create(errors)),
      value => {
        val ttype = TicketTypeModel.create(value)
        Redirect(routes.TicketType.item(ttype.id.get)).flashing("success" -> "admin.ticket_type.add.success")
      }
    )
  }

  def create = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    Ok(views.html.admin.ticket.ttype.create(typeForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val types = TicketTypeModel.list(page = page, count = count)

    Ok(views.html.admin.ticket.ttype.index(types)(request))
  }

  def edit(typeId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val ttype = TicketTypeModel.getById(typeId)

    ttype match {
      case Some(value) => Ok(views.html.admin.ticket.ttype.edit(typeId, typeForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(typeId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val ttype = TicketTypeModel.getById(typeId)

    ttype match {
      case Some(value) => Ok(views.html.admin.ticket.ttype.item(value)(request))
      case None => NotFound
    }

  }

  def update(typeId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    typeForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.ttype.edit(typeId, errors)),
      value => {
        TicketTypeModel.update(typeId, value)
        Redirect(routes.TicketType.item(typeId)).flashing("success" -> "admin.ticket_type.edit.success")
      }
    )
  }
}