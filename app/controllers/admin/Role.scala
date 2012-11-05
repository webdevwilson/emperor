package controllers.admin

import anorm._
import controllers._
import org.joda.time.DateTime
import models.RoleModel
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._

object Role extends Controller with Secured {

  val objForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "description" -> optional(text),
      "date_created" -> ignored(new DateTime())
    )(models.Role.apply)(models.Role.unapply)
  )

  def add = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    objForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.role.create(errors)),
      value => {
        val role = RoleModel.create(value)
        Redirect(routes.Role.item(role.id.get)).flashing("success" -> "admin.role.add.success")
      }
    )
  }

  def create = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    Ok(views.html.admin.role.create(objForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val roles = RoleModel.list(page = page, count = count)

    Ok(views.html.admin.role.index(roles)(request))
  }

  def edit(roleId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val role = RoleModel.getById(roleId)

    role match {
      case Some(value) => Ok(views.html.admin.role.edit(roleId, objForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(roleId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val role = RoleModel.getById(roleId)

    role match {
      case Some(value) => Ok(views.html.admin.role.item(value)(request))
      case None => NotFound
    }

  }

  def update(roleId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    objForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.role.edit(roleId, errors)),
      value => {
        RoleModel.update(roleId, value)
        Redirect(routes.Role.item(roleId)).flashing("success" -> "admin.role.edit.success")
      }
    )
  }
}