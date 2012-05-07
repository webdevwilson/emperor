package controllers

import anorm._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.libs.json.Json
import models.ProjectModel

object Project extends Controller {

  val projectForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText
    )(models.Project.apply)(models.Project.unapply)
  )

  def add = Action { implicit request =>

    projectForm.bindFromRequest.fold(
      errors => BadRequest(views.html.project.create(errors)),
      {
        case project: models.Project =>
        ProjectModel.create(project)
        Redirect("/project") // XXX
      }
    )
  }
  
  def create = Action { implicit request =>

    Ok(views.html.project.create(projectForm)(request))
  }

  def index(page: Int, count: Int) = Action { implicit request =>

    val groups = ProjectModel.list(page = page, count = count)

    Ok(views.html.project.index(groups)(request))
  }

  def edit(projectId: Long) = Action { implicit request =>

    val project = ProjectModel.findById(projectId)

    project match {
      case Some(value) => Ok(views.html.project.edit(projectId, projectForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(projectId: Long) = Action { implicit request =>
    
    val project = ProjectModel.findById(projectId)

    project match {
      case Some(value) => Ok(views.html.project.item(value)(request))
      case None => NotFound
    }
    
  }
  
  def update(projectId: Long) = Action { implicit request =>

    projectForm.bindFromRequest.fold(
      errors => BadRequest(views.html.project.edit(projectId, errors)),
      {
        case project: models.Project =>
        ProjectModel.update(projectId, project)
        Redirect("/admin") // XXX
      }
    )
  }
}