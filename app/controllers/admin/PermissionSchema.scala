package controllers.admin

import anorm._
import controllers._
import org.joda.time.DateTime
import models.{PermissionSchemeModel,ProjectModel}
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._

case class AddedPermissionSchemeGroup (
  permissionId: String,
  groupId: Long
)

case class AddedPermissionSchemeUser (
  permissionId: String,
  userId: Long
)

object PermissionScheme extends Controller with Secured {

  val objForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "description" -> optional(text),
      "date_created" -> ignored(new DateTime())
    )(models.PermissionScheme.apply)(models.PermissionScheme.unapply)
  )

  val groupForm = Form(
    mapping(
      "permission_id" -> nonEmptyText,
      "group_id" -> longNumber
    )(AddedPermissionSchemeGroup.apply)(AddedPermissionSchemeGroup.unapply)
  )

  val userForm = Form(
    mapping(
      "permission_id" -> nonEmptyText,
      "user_id" -> longNumber
    )(AddedPermissionSchemeUser.apply)(AddedPermissionSchemeUser.unapply)
  )

  def add = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    objForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.permission_scheme.create(errors)),
      value => {
        val pm = PermissionSchemeModel.create(value)
        Redirect(routes.PermissionScheme.item(pm.id.get)).flashing("success" -> "admin.permission_scheme.add.success")
      }
    )
  }

  def addGroup(id: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    groupForm.bindFromRequest.fold(
      errors => Redirect(routes.PermissionScheme.item(id)).flashing("error" -> "admin.permission_scheme.group.add.error"),
      value => {
        PermissionSchemeModel.addGroupToScheme(permissionSchemeId = id, perm = value.permissionId, groupId = value.groupId)
        Redirect(routes.PermissionScheme.item(id)).flashing("success" -> "admin.permission_scheme.group.add.success")
      }
    )
  }

  def addUser(id: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    userForm.bindFromRequest.fold(
      errors => Redirect(routes.PermissionScheme.item(id)).flashing("error" -> "admin.permission_scheme.user.add.error"),
      value => {
        PermissionSchemeModel.addUserToScheme(permissionSchemeId = id, perm = value.permissionId, userId = value.userId)
        Redirect(routes.PermissionScheme.item(id)).flashing("success" -> "admin.permission_scheme.user.add.success")
      }
    )
  }

  def create = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    Ok(views.html.admin.permission_scheme.create(objForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val pms = PermissionSchemeModel.list(page = page, count = count)
    val projects = ProjectModel.getAll(userId = request.user.id.get).groupBy(p => p.permissionSchemeId)

    Ok(views.html.admin.permission_scheme.index(pms, projects)(request))
  }

  def edit(pmId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    PermissionSchemeModel.getById(pmId) match {
      case Some(value) => Ok(views.html.admin.permission_scheme.edit(pmId, objForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(pmId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

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

  def update(pmId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    objForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.permission_scheme.edit(pmId, errors)),
      value => {
        PermissionSchemeModel.update(pmId, value)
        Redirect(routes.PermissionScheme.item(pmId)).flashing("success" -> "admin.permission_scheme.edit.success")
      }
    )
  }
}