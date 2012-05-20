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
    .verifying("auth.failure", params => UserModel.getByUsername(params.username) != None)
    .verifying("auth.failure", params => {
      val maybeUser = UserModel.getByUsername(params.username)
      maybeUser match {
        case Some(user) => {
          BCrypt.checkpw(params.password, user.password) == true          
        }
        case None => false
      }
    })
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
          Redirect("/").withSession(Security.username -> loginUser.username).flashing("success" -> "auth.success")
        }
      }
    )
  }
}

//
// https://github.com/playframework/Play20/blob/master/samples/scala/zentasks/app/controllers/Application.scala
//

/**
 * Provide security features
 */
trait Secured {
  
  /**
   * Retrieve the connected user email.
   */
  private def username(request: RequestHeader) = request.session.get("username")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.login).flashing("error" -> "auth.mustlogin")
  
  // --
  
  /** 
   * Action for authenticated users.
   */
  def IsAuthenticated(f: Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action(request => f(request))
  }
}
