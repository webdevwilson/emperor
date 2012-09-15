package models

import anorm._
import anorm.SqlParser._
import chc._
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

case class PermissionSchemeGroup(
  id: Pk[Long] = NotAssigned,
  permissionSchemeId: Long,
  permissionId: String,
  groupId: Long,
  groupName: String,
  dateCreated: Date
)

case class PermissionSchemeUser(
  id: Pk[Long] = NotAssigned,
  permissionSchemeId: Long,
  permissionId: String,
  userId: Long,
  username: String,
  realName: String,
  dateCreated: Date
)

object PermissionSchemeModel {

  val allQuery = SQL("SELECT * FROM permission_schemes")
  val allPermissionsQuery = SQL("SELECT * FROM permissions ORDER BY name")
  val deleteQuery = SQL("DELETE FROM permission_schemes WHERE id={id}")
  val deleteGroupPermQuery = SQL("DELETE FROM permission_scheme_groups WHERE permission_scheme_id={permission_scheme_id} AND permission_id={permission_id} AND group_id={group_id}")
  val deleteUserPermQuery = SQL("DELETE FROM permission_scheme_users WHERE permission_scheme_id={permission_scheme_id} AND permission_id={permission_id} AND user_id={user_id}")
  val getByIdQuery = SQL("SELECT * from permission_schemes WHERE id={id}")
  val getGroupsForPermissionQuery = SQL("SELECT * FROM permission_scheme_groups psg JOIN groups g ON psg.group_id = g.id WHERE permission_scheme_id={permission_scheme_id} AND permission_id={permission_id}")
  val getGroupsQuery = SQL("SELECT * FROM permission_scheme_groups psg JOIN groups g ON psg.group_id = g.id WHERE permission_scheme_id={permission_scheme_id}")
  val getPermForUserQuery = SQL("SELECT source FROM full_permissions WHERE project_id={project_id} AND permission_id={permission_id} AND user_id={user_id}")
  val getUsersForPermissionQuery = SQL("SELECT * FROM permission_scheme_users psu JOIN users u ON psu.user_id = u.id WHERE permission_scheme_id={permission_scheme_id} AND permission_id={permission_id}")
  val getUsersQuery = SQL("SELECT * FROM permission_scheme_users psu JOIN users u ON psu.user_id = u.id WHERE permission_scheme_id={permission_scheme_id}")
  val insertQuery = SQL("INSERT INTO permission_schemes (name, description, date_created) VALUES ({name}, {description}, UTC_TIMESTAMP())")
  val insertGroupPermQuery = SQL("INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id, date_created) VALUES ({permission_scheme_id}, {permission_id}, {group_id}, UTC_TIMESTAMP())")
  val insertUserPermQuery = SQL("INSERT INTO permission_scheme_users (permission_scheme_id, permission_id, user_id, date_created) VALUES ({permission_scheme_id}, {permission_id}, {user_id}, UTC_TIMESTAMP())")
  val listQuery = SQL("SELECT * FROM permission_schemes LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM permission_schemes")
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

  val permissionSchemeGroup = {
    get[Pk[Long]]("id") ~
    get[Long]("permission_scheme_id") ~
    get[String]("permission_id") ~
    get[Long]("group_id") ~
    get[String]("name") ~
    get[Date]("date_created") map {
      case id~permSchemeId~permId~groupId~name~dateCreated => PermissionSchemeGroup(
        id = id,
        permissionSchemeId = permSchemeId,
        permissionId = permId,
        groupId = groupId,
        groupName = name,
        dateCreated = dateCreated
      )
    }
  }

  val permissionSchemeUser = {
    get[Pk[Long]]("id") ~
    get[Long]("permission_scheme_id") ~
    get[String]("permission_id") ~
    get[Long]("user_id") ~
    get[String]("username") ~
    get[String]("realname") ~
    get[Date]("date_created") map {
      case id~permSchemeId~permId~userId~username~realName~dateCreated => PermissionSchemeUser(
        id = id,
        permissionSchemeId = permSchemeId,
        permissionId = permId,
        userId = userId,
        username = username,
        realName = realName,
        dateCreated = dateCreated
      )
    }
  }

  /**
   * Add a group permission to a scheme.
   */
  def addGroupToScheme(permissionSchemeId: Long, perm: String, groupId: Long) {

    DB.withConnection { implicit conn =>
      insertGroupPermQuery.on(
        'permission_scheme_id -> permissionSchemeId,
        'permission_id        -> perm,
        'group_id             -> groupId
      ).execute
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
      ).execute
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

  def getGroups(id: Long): List[PermissionSchemeGroup] = {

    DB.withConnection { implicit conn =>
      getGroupsQuery.on('permission_scheme_id -> id).as(permissionSchemeGroup *)
    }
  }

  def getGroupsForPermission(id: Long, permissionId: String): List[PermissionSchemeGroup] = {

    DB.withConnection { implicit conn =>
      getGroupsForPermissionQuery.on(
        'permission_scheme_id -> id,
        'permission_id -> permissionId
      ).as(permissionSchemeGroup *)
    }
  }

  def getUsers(id: Long): List[PermissionSchemeUser] = {

    DB.withConnection { implicit conn =>
      getUsersQuery.on('permission_scheme_id -> id).as(permissionSchemeUser *)
    }
  }

  def getUsersForPermission(id: Long, permissionId: String): List[PermissionSchemeUser] = {

    DB.withConnection { implicit conn =>
      getUsersQuery.on(
        'permission_scheme_id -> id,
        'permission_id -> permissionId
      ).as(permissionSchemeUser *)
    }
  }


  /**
   * Determine if the supplied user has the supplied permission in the
   * supplied project.  Returns an Option[String] that (if Some) contains a
   * String representing the actual row that granted the permission. If None
   * then the user does not have permission for the supplied project and
   * permission combination.
   */
  def hasPermission(projectId: Long, perm: String, userId: Long): Option[String] = {

    DB.withConnection { implicit conn =>
      getPermForUserQuery.on(
        'project_id     -> projectId,
        'permission_id  -> perm,
        'user_id         -> userId
      ).as(scalar[String].singleOpt)
    }
  }

  def list(page: Int = 1, count: Int = 10) : Page[PermissionScheme] = {

      val offset = count * (page - 1)

      DB.withConnection { implicit conn =>
        val pms = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(permissionScheme *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(pms, page, count, totalRows)
      }
  }

  /**
   * Remove a group from the supplied permission scheme.
   */
  def removeGroupFromScheme(permissionSchemeId: Long, perm: String, groupId: Long) = {

    DB.withConnection { implicit conn =>
      deleteGroupPermQuery.on(
        'permission_scheme_id -> permissionSchemeId,
        'permission_id        -> perm,
        'group_id             -> groupId
      ).execute
    }
  }

  /**
   * Remove a user from the supplied permission scheme.
   */
  def removeUserFromScheme(permissionSchemeId: Long, perm: String, userId: Long) = {

    DB.withConnection { implicit conn =>
      deleteUserPermQuery.on(
        'permission_scheme_id -> permissionSchemeId,
        'permission_id        -> perm,
        'user_id              -> userId
      ).execute
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