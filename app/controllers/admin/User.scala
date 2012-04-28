package controllers.admin

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._
import chc._
import org.mindrot.jbcrypt.BCrypt

object User extends Controller {

  val userForm = Form(
    tuple(
    "username" -> nonEmptyText,
    "password" -> nonEmptyText,
    "realName" -> nonEmptyText,
    "email"    -> nonEmptyText
    )
  )

  def index = Action { implicit request =>

    val users = models.UserModel.getAllUsers

    Ok(views.html.admin.user.index(users)(request))
  }
  
  def create = Action { implicit request =>

    Ok(views.html.admin.user.create(userForm)(request))
  }

  def add = Action { implicit request =>

    // val (username, password, realName, email) = userForm.bindFromRequest.get

    userForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.user.create(errors)),
      label => {
        Redirect(controllers.admin.routes.User.index)
      }
    )
  }
}