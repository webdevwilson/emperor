package controllers

import play.api._
import play.api.mvc._
import controllers.Auth._

object Admin extends Controller with Secured {
  
  def index = IsAuthenticated { implicit request =>

    Ok(views.html.admin.index(request))
  }
}