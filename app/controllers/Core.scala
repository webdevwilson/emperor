package controllers

import play.api._
import play.api.mvc._
import play.db._

object Core extends Controller {

  def index = Action { implicit request =>

    Ok(views.html.index("Your new application is ready."))
  }
}