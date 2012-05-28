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
      "date_created" -> ignored(new Date())      
    )(models.TicketType.apply)(models.TicketType.unapply)
  )

  def add = IsAuthenticated { implicit request =>

    typeForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.ttype.create(errors)),
      {
        case ttype: models.TicketType =>
        TicketTypeModel.create(ttype)
        Redirect("/admin/ticket/type") // XXX
      }
    )
  }
  
  def create = IsAuthenticated { implicit request =>

    Ok(views.html.admin.ticket.ttype.create(typeForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated { implicit request =>

    val types = TicketTypeModel.list(page = page, count = count)

    Ok(views.html.admin.ticket.ttype.index(types)(request))
  }

  def edit(typeId: Long) = IsAuthenticated { implicit request =>

    val ttype = TicketTypeModel.findById(typeId)

    ttype match {
      case Some(value) => Ok(views.html.admin.ticket.ttype.edit(typeId, typeForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(typeId: Long) = IsAuthenticated { implicit request =>
    
    val ttype = TicketTypeModel.findById(typeId)

    ttype match {
      case Some(value) => Ok(views.html.admin.ticket.ttype.item(value)(request))
      case None => NotFound
    }
    
  }
  
  def update(typeId: Long) = IsAuthenticated { implicit request =>

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