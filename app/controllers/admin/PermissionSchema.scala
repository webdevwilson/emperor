package controllers.admin

import anorm._
import controllers._
import org.joda.time.DateTime
import models.{GroupModel,PermissionSchemeModel,ProjectModel,UserModel}
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._

case class AddedPermissionSchemeGroup (
  permissionId: String,
  groupName: String
)

case class AddedPermissionSchemeUser (
  permissionId: String,
  username: String
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
      "group_name" -> nonEmptyText
    )(AddedPermissionSchemeGroup.apply)(AddedPermissionSchemeGroup.unapply)
  )

  val userForm = Form(
    mapping(
      "permission_id" -> nonEmptyText,
      "username" -> nonEmptyText
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

  def group(id: Long, permissionId: String) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val perm = PermissionSchemeModel.getPermissionById(permissionId).get

    PermissionSchemeModel.getById(id) match {
      case Some(value) =>     Ok(views.html.admin.permission_scheme.addgroup(value, perm, groupForm)(request))
      case None => NotFound
    }
  }

  def addGroup(id: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    groupForm.bindFromRequest.fold(
      errors => {
        println(errors)
        Redirect(routes.PermissionScheme.item(id)).flashing("error" -> "admin.permission_scheme.group.add.error")
      },
      value => {
        val group = GroupModel.getByName(value.groupName).get // XXX could be none!
        PermissionSchemeModel.addGroupToScheme(permissionSchemeId = id, perm = value.permissionId, groupId = group.id.get)
        Redirect(routes.PermissionScheme.item(id)).flashing("success" -> "admin.permission_scheme.group.add.success")
      }
    )
  }

  def user(id: Long, permissionId: String) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val perm = PermissionSchemeModel.getPermissionById(permissionId).get

    PermissionSchemeModel.getById(id) match {
      case Some(value) => Ok(views.html.admin.permission_scheme.adduser(value, perm, userForm)(request))
      case None => NotFound
    }
  }

  def addUser(id: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    userForm.bindFromRequest.fold(
      errors => Redirect(routes.PermissionScheme.item(id)).flashing("error" -> "admin.permission_scheme.user.add.error"),
      value => {
        val user = UserModel.getByUsername(value.username).get // XXX could be none!

        PermissionSchemeModel.addUserToScheme(permissionSchemeId = id, perm = value.permissionId, userId = user.id.get)
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

    PermissionSchemeModel.getById(pmId).map({
      value => Ok(views.html.admin.permission_scheme.item(value))
    }).getOrElse(NotFound)
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