package controllers.admin

import anorm._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._
import chc._
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
        case user: models.User =>
        UserModel.createUser(user)
        Redirect("/admin/user")
      }
    )
  }
  
  def create = Action { implicit request =>

    Ok(views.html.admin.user.create(userForm)(request))
  }

  def index(page: Int, count: Int) = Action { implicit request =>

    val users = UserModel.list(page = page, count = count)

    Ok(views.html.admin.user.index(users)(request))
  }
  
  def item(userId: Long) = Action { implicit request =>
    
    val user = UserModel.findById(userId)

    user match {
      case Some(value) => Ok(views.html.admin.user.item(value)(request))
      case None => NotFound
    }
    
  }
}