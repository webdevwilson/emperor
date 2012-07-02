package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Security._
import play.db._
import models.ProjectModel
import org.slf4j.{Logger,LoggerFactory}

object Core extends Controller with Secured {

  val logger = LoggerFactory.getLogger("application")

  def index = IsAuthenticated { implicit request =>

    models.SearchModel.checkIndices // XXX this needs to go

    val projects = models.ProjectModel.getAll

    Ok(views.html.index(projects))
  }
}