package controllers.admin

import anorm._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._
import chc._
import models.TicketTypeModel
import org.mindrot.jbcrypt.BCrypt

object TicketType extends Controller {

  val typeForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText
    )(models.TicketType.apply)(models.TicketType.unapply)
  )

  def add = Action { implicit request =>

    typeForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.ttype.create(errors)),
      {
        case ttype: models.TicketType =>
        TicketTypeModel.create(ttype)
        Redirect("/admin/ticket/type") // XXX
      }
    )
  }
  
  def create = Action { implicit request =>

    Ok(views.html.admin.ticket.ttype.create(typeForm)(request))
  }

  def index(page: Int, count: Int) = Action { implicit request =>

    val types = TicketTypeModel.list(page = page, count = count)

    Ok(views.html.admin.ticket.ttype.index(types)(request))
  }

  def edit(typeId: Long) = Action { implicit request =>

    val ttype = TicketTypeModel.findById(typeId)

    ttype match {
      case Some(value) => Ok(views.html.admin.ticket.ttype.edit(typeId, typeForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(typeId: Long) = Action { implicit request =>
    
    val ttype = TicketTypeModel.findById(typeId)

    ttype match {
      case Some(value) => Ok(views.html.admin.ticket.ttype.item(value)(request))
      case None => NotFound
    }
    
  }
  
  def update(typeId: Long) = Action { implicit request =>

    typeForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.ttype.edit(typeId, errors)),
      {
        case ttype: models.TicketType =>
        TicketTypeModel.update(typeId, ttype)
        Redirect("/admin/ticket/type") // XXX
      }
    )
  }
}