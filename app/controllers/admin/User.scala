package controllers.admin

import anorm._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.json.Json._
import play.api.mvc._
import play.db._
import chc._
import models.GroupModel
import models.UserModel
import org.mindrot.jbcrypt.BCrypt

object User extends Controller {

  val userForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "realName" -> nonEmptyText,
      "email"    -> nonEmptyText
    )(models.User.apply)(models.User.unapply)
  )
  
  def add = Action { implicit request =>

    // val (username, password, realName, email) = userForm.bindFromRequest.get

    userForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.user.create(errors)),
      {
        case user: models.User => {
          UserModel.create(user)
          Redirect("/admin/user")
        }
      }
    )
  }
  
  def addToGroup(userId: Long, groupId: Long) = Action { implicit request =>

    val user = UserModel.findById(userId)

    user match {
      case Some(value) => // #nothing
      case None => NotFound
    }

    GroupModel.addUser(userId, groupId)

    Ok(toJson(
      Map("status" -> "OK", "message" -> "admin.user.group.add.success")
    ))
  }
  
  def removeFromGroup(userId: Long, groupId: Long) = Action { implicit request =>

    val user = UserModel.findById(userId)

    user match {
      case Some(value) => // #nothing
      case None => NotFound
    }

    GroupModel.removeUser(userId, groupId)

    Ok(toJson(
      Map("status" -> "OK")
    ))
  }
  
  def create = Action { implicit request =>

    Ok(views.html.admin.user.create(userForm)(request))
  }

  def index(page: Int, count: Int) = Action { implicit request =>

    val users = UserModel.list(page = page, count = count)

    Ok(views.html.admin.user.index(users)(request))
  }

  def edit(userId: Long) = Action { implicit request =>

    val user = UserModel.findById(userId)

    user match {
      case Some(value) => Ok(views.html.admin.user.edit(userId, userForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(userId: Long) = Action { implicit request =>
    
    val user = UserModel.findById(userId)
    val allGroups = GroupModel.getAll
    val groupUsers = GroupModel.findGroupUsersForUser(userId)

    user match {
      case Some(value) => Ok(views.html.admin.user.item(value, allGroups, groupUsers)(request))
      case None => NotFound
    }
    
  }
  
  def update(userId: Long) = Action { implicit request =>

    userForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.user.edit(userId, errors)),
      {
        case user: models.User =>
        UserModel.update(userId, user)
        Redirect("/admin/user")
      }
    )
  }
}