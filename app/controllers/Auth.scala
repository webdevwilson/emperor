package controllers

import emp.event._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.Logger
import play.api.mvc._
import models._
import org.mindrot.jbcrypt.BCrypt

object Auth extends Controller {

  val loginForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginUser.apply)(LoginUser.unapply)
    .verifying("auth.failure", params => !params.username.equalsIgnoreCase("anonymous"))
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

  def login(redirectUrl: String = "/") = Action { implicit request =>

    Ok(views.html.auth.login(loginForm, redirectUrl)(request))
  }

  def logout = Action { implicit request =>

    Redirect(routes.Auth.login()).withNewSession.flashing("error" -> "auth.logout.success")
  }

  def doLogin(redirectUrl: String = "/") = Action { implicit request =>

    loginForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.auth.login(errors, redirectUrl)(request))
      }, {
        case loginUser => {

          val user = UserModel.getByUsername(loginUser.username).get // We know this exists, so just get it
          EmperorEventBus.publish(
            LogInUserEvent(
              userId = user.id.get
            )
          )

          Redirect(redirectUrl).withSession("user_id" -> user.id.get.toString).flashing("success" -> "auth.success")
        }
      }
    )
  }
}

case class AuthenticatedRequest(
  val user: User, request: Request[AnyContent]
) extends WrappedRequest(request)

/**
 * Provide security features
 */
trait Secured {

  /**
   * Redirect to login if the user in not authenticated.
   */
  private def onUnauthenticated(request: RequestHeader) = Results.Redirect("/auth/login", Map("redirectUrl" -> Seq(request.uri))).flashing("error" -> "auth.mustlogin")

  /**
   * Redirect to index if the user in not authenticated.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Core.index()).flashing("error" -> "auth.notauthorized")

  /**
   * Action for verifying authentication and authorization of users.
   */
  def IsAuthenticated(projectId: Option[Long] = None, ticketId: Option[String] = None, perm: String = "PERM_GLOBAL_LOGIN")(f: AuthenticatedRequest => Result) = {
    Action { request =>

      // Try and find a user
      val maybeUser = getUserIdFromRequest(request).flatMap({ uid =>
        UserModel.getById(uid)
      })

      maybeUser match {
        case Some(user) => {
          val maybeProjectId = if(projectId.isDefined) {
            projectId
          } else if(ticketId.isDefined) {
            // Got a ticket id.  Fetch the ticket to get the project
            TicketModel.getById(ticketId.get) match {
              case Some(ticket) => Some(ticket.projectId)
              case None => None
            }
          } else {
            // Worse case, get the core Emperor project
            Some(ProjectModel.getByKey("EMPCORE").get.id.get) // Return the default project id, this must be a global check
          }

          maybeProjectId match {
            case Some(projectId) => {
              val maybePerm = PermissionSchemeModel.hasPermission(projectId = projectId, perm = perm, userId = user.id.get)
              maybePerm match {
                case Some(cause) => {
                  Logger.debug("Granted via " + cause)
                  f(AuthenticatedRequest(user, request))
                }
                case None => {
                  Logger.debug("Denied " + perm + " to user " + user.id.get)
                  onUnauthorized(request)
                }
              }
            }
            case None => {
              Logger.debug("Denied " + perm + " to user " + user.id.get + " due to missing project.");
              onUnauthorized(request)
            }
          }
        }
        case None => {
          Logger.debug("Denied " + perm + " to unknown user");
          onUnauthenticated(request)
        }
      }
    }
  }

  /**
   * Try and find a user via header token
   */
  def getUserIdFromRequest(request: RequestHeader): Option[Long] = {

    if(request.session.get("user_id").isDefined){
      // It's in the session, use that!  This is most common, so it's first
      Some(request.session.get("user_id").get.toLong)
    } else if(request.headers.get("Authorization").isDefined && request.headers.get("Authorization").get.startsWith("Token token=")) {
      // Token is present, use that!
      // XXX Look up token!
      Some(1.toLong)
    } else {
      // Try anonymous
      val proj = ProjectModel.getByKey("EMPCORE").get
      val anon = UserModel.getIdByUsername("anonymous")

      if(anon.isDefined) {
        val anonLogin = PermissionSchemeModel.hasPermission(proj.id.get, "PERM_GLOBAL_LOGIN", anon.get)
        anonLogin match {
          case Some(cause) => Some(anon.get)
          case None => None
        }
      } else {
        None
      }
    }
  }
}
