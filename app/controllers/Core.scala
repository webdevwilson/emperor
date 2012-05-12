package controllers

import play.api._
import play.api.mvc._
import play.db._
import models.ProjectModel

object Core extends Controller {

  def index = Action { implicit request =>

    val projects = models.ProjectModel.getAll

    Ok(views.html.index(projects))
  }
}