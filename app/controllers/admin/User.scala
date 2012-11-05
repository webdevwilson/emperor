package controllers.admin

import anorm._
import controllers._
import models.GroupModel
import models.UserModel
import org.mindrot.jbcrypt.BCrypt
import org.joda.time.DateTime
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.json.Json._
import play.api.mvc._
import play.db._

object User extends Controller with Secured {

  val newForm = Form(
    mapping(
      "id"       -> ignored(NotAssigned:Pk[Long]),
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "realName" -> nonEmptyText,
      "email"    -> email,
      "timezone" -> nonEmptyText,
      "organization" -> optional(text),
      "location" -> optional(text),
      "title"    -> optional(text),
      "url"      -> optional(text),
      "date_created" -> ignored[DateTime](new DateTime())
    )(models.User.apply)(models.User.unapply)
  )

  val editForm = Form(
    mapping(
      "id"       -> ignored(NotAssigned:Pk[Long]),
      "username" -> nonEmptyText,
      "password" -> ignored[String](""),
      "realName" -> nonEmptyText,
      "email"    -> email,
      "timezone" -> nonEmptyText,
      "organization" -> optional(text),
      "location" -> optional(text),
      "title"    -> optional(text),
      "url"      -> optional(text),
      "date_created" -> ignored[DateTime](new DateTime())
    )(models.User.apply)(models.User.unapply)
  )

  val passwordForm = Form(
    mapping(
      "password" -> nonEmptyText,
      "password2"-> nonEmptyText
    )(models.NewPassword.apply)(models.NewPassword.unapply)
    verifying("admin.user.password.match", np => { np.password.equals(np.password2) })
  )

  def add = IsAuthenticated() { implicit request =>

    newForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.user.create(errors)),
      value => {
        val user = UserModel.create(value)
        Redirect(routes.User.item(user.id.get)).flashing("success" -> "admin.user.add.success")
      }
    )
  }

  def addToGroup(userId: Long, groupId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val user = UserModel.getById(userId)

    user match {
      case Some(value) => // #nothing
      case None => NotFound
    }

    GroupModel.addUser(userId, groupId)

    Ok(toJson(
      Map("status" -> "OK", "message" -> "admin.user.group.add.success")
    ))
  }

  def removeFromGroup(userId: Long, groupId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val user = UserModel.getById(userId)

    user match {
      case Some(value) => // #nothing
      case None => NotFound
    }

    GroupModel.removeUser(userId, groupId)

    Ok(toJson(
      Map("status" -> "OK")
    ))
  }

  def create = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    Ok(views.html.admin.user.create(newForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val users = UserModel.list(page = page, count = count)

    Ok(views.html.admin.user.index(users)(request))
  }

  def edit(userId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val maybeUser = UserModel.getById(userId)

    maybeUser match {
      case Some(user) => {
        Ok(views.html.admin.user.edit(userId, editForm.fill(user), passwordForm)(request))
      }
      case None => NotFound
    }
  }

  def item(userId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val user = UserModel.getById(userId)
    val allGroups = GroupModel.getAll
    val groupUsers = GroupModel.getGroupUsersForUser(userId)

    user match {
      case Some(value) => Ok(views.html.admin.user.item(value, allGroups, groupUsers)(request))
      case None => NotFound
    }

  }

  def update(userId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    editForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.user.edit(userId, errors, passwordForm)),
      {
        case user: models.User => {
          UserModel.update(userId, user)
          Redirect(routes.User.item(userId)).flashing("success" -> "admin.user.edit.success")
        }
      }
    )
  }

  def updatePassword(userId: Long) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val maybeUser = UserModel.getById(userId)

    maybeUser match {
      case Some(user) => {
        passwordForm.bindFromRequest.fold(
          errors => {
            BadRequest(views.html.admin.user.edit(userId, editForm.fill(user), errors))
          }, {
            case np: models.NewPassword => {
              UserModel.updatePassword(userId, np)
              Redirect(routes.User.item(userId)).flashing("success" -> "admin.user.password.success")
            }
          }
        )
      }
      case None => NotFound
    }
  }
}
