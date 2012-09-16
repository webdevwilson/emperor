package controllers.admin

import anorm._
import chc._
import controllers._
import java.util.Date
import models.PermissionSchemeModel
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._

object PermissionScheme extends Controller with Secured {

  val objForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "description" -> optional(text),
      "date_created" -> ignored(new Date())
    )(models.PermissionScheme.apply)(models.PermissionScheme.unapply)
  )

  def add = IsAuthorized(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    objForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.permission_scheme.create(errors)),
      value => {
        val pm = PermissionSchemeModel.create(value)
        Redirect(routes.PermissionScheme.item(pm.id.get)).flashing("success" -> "admin.permission_scheme.add.success")
      }
    )
  }

  def create = IsAuthorized(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    Ok(views.html.admin.permission_scheme.create(objForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthorized(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val pms = PermissionSchemeModel.list(page = page, count = count)

    Ok(views.html.admin.permission_scheme.index(pms)(request))
  }

  def edit(pmId: Long) = IsAuthorized(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    PermissionSchemeModel.getById(pmId) match {
      case Some(value) => Ok(views.html.admin.permission_scheme.edit(pmId, objForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(pmId: Long) = IsAuthorized(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    PermissionSchemeModel.getById(pmId) match {
      case Some(value) => {

        val perms = PermissionSchemeModel.getAllPermissions

        val pgg = PermissionSchemeModel.getGroups(pmId).groupBy( pg => pg.permissionId )
        val pgu = PermissionSchemeModel.getUsers(pmId).groupBy(pu => pu.permissionId )

        Ok(views.html.admin.permission_scheme.item(value, perms, pgg, pgu)(request))
      }
      case None => NotFound
    }

  }

  def update(pmId: Long) = IsAuthorized(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    objForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.permission_scheme.edit(pmId, errors)),
      value => {
        PermissionSchemeModel.update(pmId, value)
        Redirect(routes.PermissionScheme.item(pmId)).flashing("success" -> "admin.permission_scheme.edit.success")
      }
    )
  }
}