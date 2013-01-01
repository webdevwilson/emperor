package controllers

import anorm._
import emp.util.Search._
import org.joda.time.DateTime
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.mvc.Security._
import play.db._
import models.{PermissionSchemeModel,ProjectModel,SearchModel,UserModel,UserTokenModel}
import org.slf4j.{Logger,LoggerFactory}
import emp.util.Search._

object User extends Controller with Secured {

  val editForm = Form(
    mapping(
      "id"       -> ignored(NotAssigned:Pk[Long]),
      "username" -> nonEmptyText,
      "password" -> ignored[String](""),
      "realName" -> nonEmptyText,
      "timezone" -> nonEmptyText,
      "email"    -> email,
      "organization" -> optional(text),
      "location" -> optional(text),
      "title"    -> optional(text),
      "url"      -> optional(text),
      "date_created" -> ignored[DateTime](new DateTime())
    )(models.User.apply)(models.User.unapply)
  )

  val passwordForm = Form(
    mapping(
      "password" -> nonEmptyText,
      "password2"-> nonEmptyText
    )(models.NewPassword.apply)(models.NewPassword.unapply)
    verifying("user.password.match", np => { np.password.equals(np.password2) })
  )

  val tokenForm = Form(
    mapping(
      "token"   -> ignored(NotAssigned:Pk[String]),
      "userId"  -> ignored[Long](1),
      "comment" -> optional(text),
      "dateCreated" -> ignored[DateTime](new DateTime())
    )(models.UserToken.apply)(models.UserToken.unapply)
  )

  def edit(userId: Long) = IsAuthenticated() { implicit request =>

    // A lot of stupid code just to verify that this is either an admin or a
    // user. Ugh.
    val cproj = ProjectModel.getByKey("EMPCORE").get
    val isAdmin = PermissionSchemeModel.hasPermission(
      cproj.id.get, "PERM_GLOBAL_ADMIN", request.user.id.get
    )
    val canEdit = if(isAdmin.isDefined || request.user.id.get == userId)
      true else false

    if(canEdit) {
      val maybeUser = UserModel.getById(userId)

      maybeUser match {
        case Some(user) => {
          Ok(views.html.user.edit(userId, editForm.fill(user), tokenForm, passwordForm)(request))
        }
        case None => NotFound
      }
    } else {
      Results.Redirect(routes.Core.index()).flashing("error" -> "auth.notauthorized")
    }
  }

  def generateToken(userId: Long) = IsAuthenticated() { implicit request =>
    UserModel.getById(userId).map({ user =>
      tokenForm.bindFromRequest.fold(
        errors => {
          Results.Redirect(routes.Core.index()).flashing("error" -> "user.token.failure")
        }, {
          case token: models.UserToken => {
            UserTokenModel.create(userId, token.comment)
            Results.Redirect(routes.User.edit(userId)).flashing("success" -> "user.token.success")
          }
        }
      )
    }).getOrElse(NotFound)
  }

  def item(userId: Long, page: Int = 1, count: Int = 10) = IsAuthenticated() { implicit request =>

    val maybeUser = UserModel.getById(userId)

    maybeUser match {
      case Some(user) => {
        val efilters = Map("user_id" -> Seq(userId.toString))

        val eventQuery = SearchQuery(userId = request.user.id.get, filters = efilters, page = page, count = count)

        val events = SearchModel.searchEvent(eventQuery)

        Ok(views.html.user.item(user, events)(request))
      }
      case None => NotFound
    }
  }

  def update(userId: Long) = IsAuthenticated() { implicit request =>

    // A lot of stupid code just to verify that this is either an admin or a
    // user. Ugh.
    val cproj = ProjectModel.getByKey("EMPCORE").get
    val isAdmin = PermissionSchemeModel.hasPermission(
      cproj.id.get, "PERM_GLOBAL_ADMIN", request.user.id.get
    )
    val canEdit = if(isAdmin.isDefined || request.user.id.get == userId)
      true else false

    if(canEdit) {

      editForm.bindFromRequest.fold(
        errors => {
          BadRequest(views.html.user.edit(userId, errors, tokenForm, passwordForm))
        },
        {
          case user: models.User => {
            UserModel.update(userId, user)
            Redirect(routes.User.item(userId)).flashing("success" -> "user.edit.success")
          }
        }
      )
    } else {
      Results.Redirect(routes.Core.index()).flashing("error" -> "auth.notauthorized")
    }
  }

  def updatePassword(userId: Long) = IsAuthenticated() { implicit request =>

    // A lot of stupid code just to verify that this is either an admin or a
    // user. Ugh.
    val cproj = ProjectModel.getByKey("EMPCORE").get
    val isAdmin = PermissionSchemeModel.hasPermission(
      cproj.id.get, "PERM_GLOBAL_ADMIN", request.user.id.get
    )
    val canEdit = if(isAdmin.isDefined || request.user.id.get == userId)
      true else false

    if(canEdit) {
      val maybeUser = UserModel.getById(userId)

      maybeUser match {
        case Some(user) => {
          passwordForm.bindFromRequest.fold(
            errors => {
              BadRequest(views.html.user.edit(userId, editForm.fill(user), tokenForm, errors))
            }, {
              case np: models.NewPassword => {
                UserModel.updatePassword(userId, np)
                Redirect(routes.User.item(userId)).flashing("success" -> "user.password.success")
              }
            }
          )
        }
        case None => NotFound
      }
    } else {
      Results.Redirect(routes.Core.index()).flashing("error" -> "auth.notauthorized")
    }
  }
}
