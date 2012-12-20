package controllers

import emp._
import emp.util.Search._
import play.api._
import play.api.mvc._
import play.api.mvc.Security._
import play.db._
import models.{SearchModel,UserModel}
import org.slf4j.{Logger,LoggerFactory}
import emp.util.Search._

object User extends Controller with Secured {

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
}
