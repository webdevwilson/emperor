package controllers

import play.api._
import play.api.mvc._
import play.db._
import org.mindrot.jbcrypt.BCrypt

object Core extends Controller {

  def index = Action { implicit request =>

    // val list = models.TicketModel.getAllTypes

    val foo = BCrypt.hashpw("testing", BCrypt.gensalt(12))

    Ok(views.html.index("Your new application is ready.", foo))
  }
}