package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc._

object Stack extends Controller with Secured {

  def index = IsAuthenticated { implicit request =>

    Ok(views.html.stack.index()(request))
  }

}