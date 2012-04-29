package controllers.admin

import anorm._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._
import chc._
import models.GroupModel
import org.mindrot.jbcrypt.BCrypt

object Group extends Controller {

  val groupForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText
    )(models.Group.apply)(models.Group.unapply)
  )

  def add = Action { implicit request =>

    groupForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.group.create(errors)),
      {
        case group: models.Group =>
        GroupModel.create(group)
        Redirect("/admin/group") // XXX
      }
    )
  }
  
  def create = Action { implicit request =>

    Ok(views.html.admin.group.create(groupForm)(request))
  }

  def index(page: Int, count: Int) = Action { implicit request =>

    val groups = GroupModel.list(page = page, count = count)

    Ok(views.html.admin.group.index(groups)(request))
  }

  def edit(groupId: Long) = Action { implicit request =>

    val group = GroupModel.findById(groupId)

    group match {
      case Some(value) => Ok(views.html.admin.group.edit(groupId, groupForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(groupId: Long) = Action { implicit request =>
    
    val group = GroupModel.findById(groupId)

    group match {
      case Some(value) => Ok(views.html.admin.group.item(value)(request))
      case None => NotFound
    }
    
  }
  
  def update(groupId: Long) = Action { implicit request =>

    groupForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.group.edit(groupId, errors)),
      {
        case group: models.Group =>
        GroupModel.update(groupId, group)
        Redirect("/admin/group") // XXX
      }
    )
  }
}