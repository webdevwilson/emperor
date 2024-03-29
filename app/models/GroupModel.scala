package models

import anorm._
import anorm.SqlParser._
import emp.util.AnormExtension._
import emp.util.Pagination.Page
import org.joda.time.DateTime
import play.api.db.DB
import play.api.Play.current
import play.Logger

/**
 * Class for groups.
 */
case class Group(id: Pk[Long] = NotAssigned, name: String, dateCreated: DateTime = DateTime.now)

/**
 * Class for users in a group.
 */
case class GroupUser(
  id: Pk[Long] = NotAssigned,
  userId: Long,
  username: String,
  realName: String,
  groupId: Long,
  dateCreated: DateTime
)

object GroupModel {

  val addUserQuery = SQL("INSERT INTO group_users (user_id, group_id) VALUES ({userId}, {groupId})")
  val removeUserQuery = SQL("DELETE FROM group_users WHERE user_id={userId} AND group_id={groupId}")
  val allQuery = SQL("SELECT * FROM groups")
  val allGroupUsersForGroupQuery = SQL("SELECT * FROM group_users gu JOIN users u ON u.id = gu.user_id WHERE group_id={groupId} ORDER BY u.username")
  val allGroupUsersForUserQuery = SQL("SELECT * FROM group_users gu JOIN users u ON u.id = gu.user_id WHERE user_id={userId}")
  val allForUserQuery = SQL("SELECT * FROM groups g JOIN group_users gu ON g.id = gu.group_id WHERE gu.user_id={userId}")
  val startsWithQuery = SQL("SELECT * FROM groups WHERE name LIKE {name}")
  val getByIdQuery = SQL("SELECT * FROM groups WHERE id={id}")
  val getByNameQuery = SQL("SELECT * FROM groups WHERE name={name}")
  val getGoupUserByIdQuery = SQL("SELECT * FROM group_users gu JOIN users u ON u.id = gu.user_id WHERE gu.id={id}")
  val listQuery = SQL("SELECT * FROM groups ORDER BY name LIMIT {count} OFFSET {offset}")
  val listCountQuery = SQL("SELECT count(*) FROM groups")
  val insertQuery = SQL("INSERT INTO groups (name) VALUES ({name})")
  val updateQuery = SQL("UPDATE groups SET name={name} WHERE id={id}")
  val deleteQuery = SQL("DELETE FROM groups WHERE id={id}")

  // parser for retrieving a group
  val group = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[DateTime]("date_created") map {
      case id~name~dateCreated => Group(id, name, dateCreated)
    }
  }

  // parser for retrieving a group user
  val groupUser = {
    get[Pk[Long]]("id") ~
    get[Long]("group_id") ~
    get[Long]("user_id") ~
    get[String]("username") ~
    get[String]("realname") ~
    get[DateTime]("date_created") map {
      case id~group_id~user_id~username~realname~date_created => GroupUser(
        id = id,
        groupId = group_id,
        userId = user_id,
        username = username,
        realName = realname,
        dateCreated = date_created
      )
    }
  }

  /**
   * Add user to group.
   */
  def addUser(userId: Long, groupId: Long): Option[GroupUser] = {

    DB.withConnection { implicit conn =>
      val id = addUserQuery.on(
        'userId -> userId,
        'groupId-> groupId
      ).executeInsert()
      getGoupUserByIdQuery.on('id -> id).as(groupUser.singleOpt)
    }
  }

  /**
   * Create a group.
   */
  def create(group: Group): Group = {

    val id = DB.withConnection { implicit conn =>
      insertQuery.on(
        'name   -> group.name
      ).executeInsert()
    }
    group.copy(id = new Id(id.get))
  }

  /**
   * Delete a group.
   */
  def delete(id: Long) {
      DB.withConnection { implicit conn =>
        deleteQuery.on(
          'id -> id
        ).execute
      }
  }

  /**
   * Retrieve a group by id.
   */
  def getById(id: Long) : Option[Group] = {

    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(group.singleOpt)
    }
  }

  /**
   * Retrieve a group by name.
   */
  def getByName(name: String) : Option[Group] = {

    DB.withConnection { implicit conn =>
      getByNameQuery.on('name -> name).as(group.singleOpt)
    }
  }

  /**
   * Find all groups starting with a specific string. Used for
   * autocomplete.
   */
  def getStartsWith(query: String) : Seq[Group] = {

    val likeQuery = query + "%"

    DB.withConnection { implicit conn =>
      startsWithQuery.on(
        'name -> likeQuery
      ).as(group.*)
    }
  }

  def getAll: List[Group] = {

    DB.withConnection { implicit conn =>
      allQuery.as(group.*)
    }
  }

  /**
   * Get the GroupUsers that this user is in.
   */
  def getGroupUsersForUser(userId: Long): List[GroupUser] = {

    DB.withConnection { implicit conn =>
      allGroupUsersForUserQuery.on('userId -> userId).as(groupUser.*)
    }
  }

  /**
   * Get the users that are in this group.
   */
  def getGroupUsersForGroup(groupId: Long): List[GroupUser] = {

    DB.withConnection { implicit conn =>
      allGroupUsersForGroupQuery.on('groupId -> groupId).as(groupUser.*)
    }
  }

  /**
   * Get the groups that this user is in.
   */
  def getForUser(userId: Long): List[Group] = {

    DB.withConnection { implicit conn =>
      allForUserQuery.on('userId -> userId).as(group.*)
    }
  }

  def list(page: Int = 1, count: Int = 10) : Page[Group] = {

      val offset = count * (page - 1)

      DB.withConnection { implicit conn =>
        val groups = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(group.*)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(groups, page, count, totalRows)
      }
  }

  /**
   * Remove a user from a group.
   */
  def removeUser(userId : Long, groupId : Long) {

    DB.withConnection { implicit conn =>
      removeUserQuery.on(
        'userId   -> userId,
        'groupId  -> groupId
      ).execute
    }
  }

  /**
   * Update a group.  Returns the changed group.
   */
  def update(id: Long, group: Group): Option[Group] = {

    DB.withConnection { implicit conn =>
      updateQuery.on(
        'id         -> id,
        'name       -> group.name
      ).execute
      getById(id)
    }
  }
}
