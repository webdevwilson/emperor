package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models.LoginUser
import models.UserModel
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.{Logger,LoggerFactory}

object Auth extends Controller {

  val logger = LoggerFactory.getLogger("application")

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
        case loginUser => {
          val maybeUser = UserModel.getByUsername(loginUser.username)
          maybeUser match {
            case Some(user) => {
              if(BCrypt.checkpw(loginUser.password, user.password)) {
                Redirect("/").withSession(Security.username -> user.username).flashing("success" -> "auth.success") // XXX
              } else {
                BadRequest(views.html.auth.login(loginForm)(request)).flashing("error" -> "auth.failure") // XXX no redirect
              }
            }
            case None => {
              BadRequest(views.html.auth.login(loginForm)(request)).flashing("error" -> "auth.failure") // XXX no redirect
            }
          }
        }
      }
    )
  }
}