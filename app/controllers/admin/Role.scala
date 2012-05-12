package controllers.admin

import anorm._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._
import chc._
import models.RoleModel

object Role extends Controller {

  val objForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "description" -> optional(text)
    )(models.Role.apply)(models.Role.unapply)
  )

  def add = Action { implicit request =>

    objForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.role.create(errors)),
      {
        case role: models.Role =>
        RoleModel.create(role)
        Redirect("/admin/role") // XXX
      }
    )
  }
  
  def create = Action { implicit request =>

    Ok(views.html.admin.role.create(objForm)(request))
  }

  def index(page: Int, count: Int) = Action { implicit request =>

    val roles = RoleModel.list(page = page, count = count)

    Ok(views.html.admin.role.index(roles)(request))
  }

  def edit(roleId: Long) = Action { implicit request =>

    val role = RoleModel.findById(roleId)

    role match {
      case Some(value) => Ok(views.html.admin.role.edit(roleId, objForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(roleId: Long) = Action { implicit request =>
    
    val role = RoleModel.findById(roleId)

    role match {
      case Some(value) => Ok(views.html.admin.role.item(value)(request))
      case None => NotFound
    }
    
  }
  
  def update(roleId: Long) = Action { implicit request =>

    objForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.role.edit(roleId, errors)),
      {
        case role: models.Role =>
        RoleModel.update(roleId, role)
        Redirect("/admin/role") // XXX
      }
    )
  }
}