package controllers

import play.api._
import play.api.mvc._
import play.db._
import chc._
import org.mindrot.jbcrypt.BCrypt
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.ql._

object Core extends Controller {

  def index = Action { implicit request =>

    val list = models.TicketModel.getAllTypes

    val foo = BCrypt.hashpw("testing", BCrypt.gensalt(12))

    Ok(views.html.index("Your new application is ready.", list, foo))
  }
}