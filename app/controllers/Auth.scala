package controllers

import emp._
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
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Core.index).flashing("error" -> "auth.notauthorized")

  /**
   * Action for verifying authentication and authorization of users.
   */
  def IsAuthenticated(projectId: Option[Long] = None, ticketId: Option[String] = None, perm: String = "PERM_GLOBAL_LOGIN")(f: AuthenticatedRequest => Result) = {
    Action { request =>

      // First grab the user_id from the session, maybe
      val maybeUserId = request.session.get("user_id")

      // Try and fetch the user with the specified name. If we find it, great.
      // If we don't then check to see if we allow login by the anonymous
      // user.
      val maybeUser: Option[User] = maybeUserId.map(userId => UserModel.getById(userId.toLong)).getOrElse({
        val proj = ProjectModel.getByKey("EMPCORE").get
        val anon = UserModel.getByUsername("anonymous")
        val anonLogin = PermissionSchemeModel.hasPermission(proj.id.get, "PERM_GLOBAL_LOGIN", anon.get.id.get)
        anonLogin match {
          case Some(cause) => anon
          case None => None
        }
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
                case Some(cause) => f(AuthenticatedRequest(user, request))
                case None => onUnauthorized(request)
              }
            }
            case None => onUnauthorized(request)
          }
        }
        case None => onUnauthenticated(request)
      }
    }
  }
}
