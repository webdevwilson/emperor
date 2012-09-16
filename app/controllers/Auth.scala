package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models.{LoginUser,PermissionSchemeModel,UserModel}
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.{Logger,LoggerFactory}

object Auth extends Controller {

  val logger = LoggerFactory.getLogger("application")

  val loginForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginUser.apply)(LoginUser.unapply)
    // XXX This whole login block could be replaced by a single method that checks everything and returns a boolean
    // XXX could eliminate one of these by combining, reducing one of the queries
    .verifying("auth.failure", params => UserModel.getByUsername(params.username) != None)
    .verifying("auth.failure", params => {
      val maybeUser = UserModel.getByUsername(params.username)
      maybeUser match {
        case Some(user) => {
          BCrypt.checkpw(params.password, user.password) == true && user.id != 0 // XXX The 0 disallows Anonymous logging in
        }
        case None => false
      }
    })
  )

  def login = Action { implicit request =>

    Ok(views.html.auth.login(loginForm)(request))
  }

  def logout = Action { implicit request =>

    Redirect(routes.Auth.login).withNewSession.flashing("error" -> "auth.logout.success")
  }

  def doLogin = Action { implicit request =>

    loginForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.auth.login(errors)(request))
      }, {
        case loginUser => {

          val user = UserModel.getByUsername(loginUser.username).get // We know this exists, so just get it

          Redirect(routes.Core.index).withSession("user_id" -> user.id.get.toString).flashing("success" -> "auth.success")
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
   * Retrieve the current username.
   */
  private def username(request: RequestHeader): Option[String] = {
    val maybeUser = request.session.get("user_id")
    maybeUser match {
      case Some(user) => maybeUser
      case None => {
        val ps = PermissionSchemeModel.getByName("EMP_PERM_SCHEME_CORE").get // XXX Some sort of settings table to hold this information?
        val maybePerm = PermissionSchemeModel.hasPermission(ps.id.get, "PERM_GLOBAL_LOGIN", 0) // Can Anonymous log in?
        maybePerm match {
          case Some(cause) => Some("0") // XXX Should log "cause" here
          case None => None
        }
      }
    }
  }

  /**
   * Redirect to login if the user in not authenticated.
   */
  private def onUnauthenticated(request: RequestHeader) = Results.Redirect(routes.Auth.login).flashing("error" -> "auth.mustlogin")

  /**
   * Redirect to index if the user in not authenticated.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Core.index).flashing("error" -> "auth.notauthorized")

  /**
   * Action for authenticated users.
   */
  def IsAuthenticated(f: Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthenticated) { user =>
    Action(request => f(request))
  }

  def IsAuthorized(projectId: Long, reqPerm: String)(f: Request[AnyContent] => Result) = IsAuthenticated { request =>

    val maybeUser = username(request)
    maybeUser match {
      case Some(user) => {
        val maybePerm = PermissionSchemeModel.hasPermission(projectId = projectId, perm = reqPerm, userId = user.toLong)
        maybePerm match {
          case Some(cause) => f(request) // Log the cause!
          case None => onUnauthorized(request)
        }
      }
      case None => onUnauthenticated(request)
    }
  }
}
