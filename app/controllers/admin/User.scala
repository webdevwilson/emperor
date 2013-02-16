package controllers.admin

import anorm._
import controllers._
import models.GroupModel
import models.UserModel
import org.mindrot.jbcrypt.BCrypt
import org.joda.time.{DateTime,DateTimeZone}
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.json.Json._
import play.api.mvc._
import play.api.Play.current
import play.api.db._

object User extends Controller with Secured {

  val newForm = Form(
    mapping(
      "id"       -> ignored(NotAssigned:Pk[Long]),
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "realName" -> nonEmptyText,
      "timezone" -> nonEmptyText,
      "email"    -> email,
      "organization" -> optional(text),
      "location" -> optional(text),
      "title"    -> optional(text),
      "url"      -> optional(text),
      "date_created" -> ignored[DateTime](new DateTime())
    )(models.User.apply)(models.User.unapply)
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

    // Create a default user for filling in the form with the
    // timezone, which we will default to Joda's "default" time.
    val defaultUser = models.User(
      id = Id(1.toLong),
      username = "",
      password = "",
      realName = "",
      timezone = Play.configuration.getString("emperor.timezone").getOrElse(DateTimeZone.getDefault().getID),
      email = "",
      organization = None,
      location = None,
      title = None,
      url = None,
      dateCreated = new DateTime()
    )

    Ok(views.html.admin.user.create(newForm.fill(defaultUser))(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val users = UserModel.list(page = page, count = count)

    Ok(views.html.admin.user.index(users)(request))
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
}
