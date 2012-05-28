package controllers.admin

import anorm._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.mvc._
import play.db._
import chc._
import controllers._
import java.util.Date
import models.GroupModel
import models.UserModel

object Group extends Controller with Secured {

  val addForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "date_created" -> ignored(new Date())
    )(models.Group.apply)(models.Group.unapply)
  )

  def add = IsAuthenticated { implicit request =>

    addForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.group.create(errors)),
      {
        case group: models.Group =>
        GroupModel.create(group)
        Redirect("/admin/group") // XXX
      }
    )
  }
  
  def create = IsAuthenticated { implicit request =>

    Ok(views.html.admin.group.create(addForm)(request))
  }

  def index(page: Int, count: Int) = IsAuthenticated { implicit request =>

    val groups = GroupModel.list(page = page, count = count)

    Ok(views.html.admin.group.index(groups)(request))
  }

  def edit(groupId: Long) = IsAuthenticated { implicit request =>

    val group = GroupModel.getById(groupId)

    group match {
      case Some(value) => Ok(views.html.admin.group.edit(groupId, addForm.fill(value))(request))
      case None => NotFound
    }
  }

  def item(groupId: Long) = IsAuthenticated { implicit request =>
    
    val group = GroupModel.getById(groupId)
    val allUsers = UserModel.getAll
    val userGroups = GroupModel.getGroupUsersForGroup(groupId)

    group match {
      case Some(value) => Ok(views.html.admin.group.item(value, allUsers, userGroups)(request))
      case None => NotFound
    }
    
  }
  
  def update(groupId: Long) = IsAuthenticated { implicit request =>

    addForm.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.group.edit(groupId, errors)),
      {
        case group: models.Group =>
        GroupModel.update(groupId, group)
        Redirect("/admin/group") // XXX
      }
    )
  }
}