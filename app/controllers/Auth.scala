package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models.LoginUser

object Auth extends Controller {

  val loginForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginUser.apply)(LoginUser.unapply)
  )

  def login = Action { implicit request =>

    Ok(views.html.auth.login(loginForm)(request))
  }

  def doLogin = Action { implicit request =>

    loginForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.auth.login(errors)(request))
      }, {
        case loginUser => Redirect("/") // XXX
      }
    )
  }
}