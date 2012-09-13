package models

import anorm._
import anorm.SqlParser._
import java.util.Date
import play.api.db.DB
import play.api.Play.current

case class Permission(
  name: String
)

case class PermissionScheme(
  id: Pk[Long] = NotAssigned,
  name: String,
  description: Option[String],
  dateCreated: Date
)

object PermissionSchemeModel {

  val allQuery = SQL("SELECT * FROM permission_schemes")
  val allPermissionsQuery = SQL("SELECT * FROM permissions")
  val deleteQuery = SQL("DELETE FROM permission_schemes WHERE id={id}")
  val getByIdQuery = SQL("SELECT * from permission_schemes WHERE id={id}")
  val insertQuery = SQL("INSERT INTO permission_schemes (name, description, date_created) VALUES ({name}, {description}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE permission_schemes SET name={name}, description={description} WHERE id={id}")

  val permission = {
    get[String]("name") map {
      case name => Permission(name = name)
    }
  }

  val permissionScheme = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[Option[String]]("description") ~
    get[Date]("date_created") map {
      case id~name~description~dateCreated => PermissionScheme(
        id = id,
        name = name,
        description = description,
        dateCreated = dateCreated
      )
    }
  }

  def create(ps: PermissionScheme): PermissionScheme = {

    DB.withConnection { implicit conn =>
      val id = insertQuery.on(
        'name         -> ps.name,
        'description  -> ps.description
      ).executeInsert()

      this.getById(id.get).get
    }
  }

  def delete(id: Long) {
    DB.withConnection { implicit conn =>
      deleteQuery.on('id -> id).execute
    }
  }

  def getAll: List[PermissionScheme] = {

    DB.withConnection { implicit conn =>
      allQuery.as(permissionScheme *)
    }
  }

  def getAllPermissions: List[Permission] = {

    DB.withConnection { implicit conn =>
      allPermissionsQuery.as(permission *)
    }
  }

  def getById(id: Long): Option[PermissionScheme] = {

    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(permissionScheme.singleOpt)
    }
  }

  def update(id: Long, ps: PermissionScheme) = {

    DB.withConnection { implicit conn =>
      updateQuery.on(
        'id           -> id,
        'name         -> ps.name,
        'description  -> ps.description
      ).execute
      getById(id)
    }
  }
}