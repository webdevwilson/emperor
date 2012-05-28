package controllers.admin

import anorm._
import chc._
import controllers._
import java.util.Date
import models.TicketLinkTypeModel
import org.mindrot.jbcrypt.BCrypt
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._

object TicketLinkType extends Controller with Secured {

  val ltypeForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "date_created" -> ignored(new Date())
    )(models.TicketLinkType.apply)(models.TicketLinkType.unapply)
  )

  def add = IsAuthenticated { implicit request =>

    ltypeForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.linktype.create(errors)),
      {
        case tltype: models.TicketLinkType =>
        TicketLinkTypeModel.create(tltype)
        Redirect("/admin/ticket/linktype") // XXX
      }
    )
  }
  
  def create = IsAuthenticated { implicit request =>

    Ok(views.html.admin.ticket.linktype.create(ltypeForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated { implicit request =>

    val types = TicketLinkTypeModel.list(page = page, count = count)

    Ok(views.html.admin.ticket.linktype.index(types)(request))
  }

  def edit(ltypeId: Long) = IsAuthenticated { implicit request =>

    val tltype = TicketLinkTypeModel.getById(ltypeId)

    tltype match {
      case Some(value) => Ok(views.html.admin.ticket.linktype.edit(ltypeId, ltypeForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(ltypeId: Long) = IsAuthenticated { implicit request =>
    
    val tltype = TicketLinkTypeModel.getById(ltypeId)

    tltype match {
      case Some(value) => Ok(views.html.admin.ticket.linktype.item(value)(request))
      case None => NotFound
    }
    
  }
  
  def update(ltypeId: Long) = IsAuthenticated { implicit request =>

    ltypeForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.ticket.linktype.edit(ltypeId, errors)),
      {
        case ttype: models.TicketLinkType =>
        TicketLinkTypeModel.update(ltypeId, ttype)
        Redirect("/admin/ticket/linktype") // XXX
      }
    )
  }
}