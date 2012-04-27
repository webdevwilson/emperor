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
      "username" -> of[String],
      "password" -> of[String],
      "realName" -> of[String],
      "email"    -> of[String]
    )
  )

  def index = Action { implicit request =>

    Ok(views.html.admin.user.index(request))
  }
  
  def create = Action { implicit request =>

    Ok(views.html.admin.user.create(request))
  }

  def add = Action { implicit request =>

    val (username) = userForm.bindFromRequest.get

    Ok(views.html.admin.user.create(request))
  }
}