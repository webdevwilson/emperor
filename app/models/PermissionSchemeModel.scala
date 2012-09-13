package models

import anorm._
import anorm.SqlParser._
import java.util.Date
import play.api.db.DB
import play.api.Play.current

/**
 * Class for a permission.
 */
case class Permission(
  name: String
)

/**
 * Class for a permission scheme.
 */
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
  val getPermForUserQuery = SQL("SELECT count(*) FROM full_permissions WHERE project_id={project_id} AND permission_id={permission_id} AND user_id={user_id}")
  val insertQuery = SQL("INSERT INTO permission_schemes (name, description, date_created) VALUES ({name}, {description}, UTC_TIMESTAMP())")
  val insertGroupPermQuery = SQL("INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id, date_created) VALUES ({permission_scheme_id}, {permission_id}, {group_id}, UTC_TIMESTAMP())")
  val insertUserPermQuery = SQL("INSERT INTO permission_scheme_users (permission_scheme_id, permission_id, user_id, date_created) VALUES ({permission_scheme_id}, {permission_id}, {user_id}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE permission_schemes SET name={name}, description={description} WHERE id={id}")

  // Parser for retrieving a permission
  val permission = {
    get[String]("name") map {
      case name => Permission(name = name)
    }
  }

  // Parser for retrieving a permission scheme
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

  /**
   * Add a group permission to a scheme.
   */
  def addGroupToScheme(permissionSchemeId: Long, perm: String, groupId: Long) {

    DB.withConnection { implicit conn =>
      insertUserPermQuery.on(
        'permission_scheme_id -> permissionSchemeId,
        'permission_id        -> perm,
        'group_id             -> groupId
      )
    }
  }

  /**
   * Add a user permission to a scheme.
   */
  def addUserToScheme(permissionSchemeId: Long, perm: String, userId: Long) {

    DB.withConnection { implicit conn =>
      insertUserPermQuery.on(
        'permission_scheme_id -> permissionSchemeId,
        'permission_id        -> perm,
        'user_id              -> userId
      )
    }
  }

  /**
   * Create a new PermissionScheme
   */
  def create(ps: PermissionScheme): PermissionScheme = {

    DB.withConnection { implicit conn =>
      val id = insertQuery.on(
        'name         -> ps.name,
        'description  -> ps.description
      ).executeInsert()

      this.getById(id.get).get
    }
  }

  /**
   * Delete permission schema.
   */
  def delete(id: Long) {
    DB.withConnection { implicit conn =>
      deleteQuery.on('id -> id).execute
    }
  }

  /**
   * Get all permission schemes.
   */
  def getAll: List[PermissionScheme] = {

    DB.withConnection { implicit conn =>
      allQuery.as(permissionScheme *)
    }
  }

  /**
   * Get all permissions.
   */
  def getAllPermissions: List[Permission] = {

    DB.withConnection { implicit conn =>
      allPermissionsQuery.as(permission *)
    }
  }

  /**
   * Get a permissions scheme by id.
   */
  def getById(id: Long): Option[PermissionScheme] = {

    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(permissionScheme.singleOpt)
    }
  }

  /**
   * Determine if the supplied user has the supplied permission in the
   * supplied project.
   */
  def hasPermission(projectId: Long, perm: String, userId: Long): Boolean = {

    DB.withConnection { implicit conn =>
      val count = getPermForUserQuery.on(
        'project_id     -> projectId,
        'permission_id  -> perm,
        'user_id         -> userId
      ).as(scalar[Long].single)
      if(count > 0) {
        true
      } else {
        false
      }
    }
  }

  /**
   * Update a permission scheme
   */
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