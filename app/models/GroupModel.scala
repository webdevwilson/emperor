package models

import anorm._
import anorm.SqlParser._
import chc._
import java.util.Date
import play.api.db.DB
import play.api.Play.current
import play.Logger

case class Group(id: Pk[Long] = NotAssigned, name: String, dateCreated: Date)

case class GroupUser(id: Pk[Long] = NotAssigned, user_id: Long, group_id: Long)

object GroupModel {

  val addUserQuery = SQL("INSERT IGNORE INTO group_users (user_id, group_id) VALUES ({userId}, {groupId})")
  val removeUserQuery = SQL("DELETE FROM group_users WHERE user_id={userId} AND group_id={groupId}")
  val allQuery = SQL("SELECT * FROM groups")
  val allGroupUsersForGroupQuery = SQL("SELECT * FROM group_users WHERE group_id={groupId}")
  val allGroupUsersForUserQuery = SQL("SELECT * FROM group_users WHERE user_id={userId}")
  val allForUserQuery = SQL("SELECT * FROM groups g JOIN group_users gu ON g.id = gu.id WHERE gu.user_id={userId}")
  val startsWithQuery = SQL("SELECT * FROM groups WHERE name LIKE {name}")
  val getByIdQuery = SQL("SELECT * FROM groups WHERE id={id}")
  val listQuery = SQL("SELECT * FROM groups LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM groups")
  val insertQuery = SQL("INSERT INTO groups (name, date_created) VALUES ({name}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE groups SET name={name} WHERE id={id}")

  val group = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[Date]("date_created") map {
      case id~name~dateCreated => Group(id, name, dateCreated)
    }
  }

  val groupUser = {
    get[Pk[Long]]("id") ~
    get[Long]("group_id") ~
    get[Long]("user_id") map {
      case id~group_id~user_id => GroupUser(id, group_id, user_id)
    }
  }

  def addUser(userId: Long, groupId: Long) {

    DB.withConnection { implicit conn =>
      addUserQuery.on(
        'userId -> userId,
        'groupId-> groupId
      ).execute
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

  def delete(id: Long) {
      // XXX
  }

  /**
   * Retrieve a group by id.
   */
  def getById(id: Long) : Option[Group] = {

    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(group.singleOpt)
    }
  }

  def getStartsWith(query: String) : Seq[Group] = {

    val likeQuery = query + "%"

    DB.withConnection { implicit conn =>
      val groups = startsWithQuery.on(
        'name -> likeQuery
      ).as(group *)

      groups
    }
  }

  def getAll: List[Group] = {

    DB.withConnection { implicit conn =>
      allQuery.as(group *)
    }
  }

  def getGroupUsersForUser(userId: Long): List[GroupUser] = {

    DB.withConnection { implicit conn =>
      allGroupUsersForUserQuery.on('userId -> userId).as(groupUser *)
    }
  }

  def getGroupUsersForGroup(groupId: Long): List[GroupUser] = {

    DB.withConnection { implicit conn =>
      allGroupUsersForGroupQuery.on('groupId -> groupId).as(groupUser *)
    }
  }

  def getForUser(userId: Long): List[Group] = {

    DB.withConnection { implicit conn =>
      allForUserQuery.on('userId -> userId).as(group *)
    }
  }

  def list(page: Int = 1, count: Int = 10) : Page[Group] = {

      val offset = count * (page - 1)

      DB.withConnection { implicit conn =>
        val groups = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(group *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(groups, page, count, totalRows)
      }
  }

  def removeUser(userId : Long, groupId : Long) {

    DB.withConnection { implicit conn =>
      removeUserQuery.on(
        'userId   -> userId,
        'groupId  -> groupId
      ).execute
    }
  }

  def update(id: Long, group: Group) = {

    DB.withTransaction { implicit conn =>
      val foo = updateQuery.on(
        'id         -> id,
        'name       -> group.name
      ).executeUpdate
    }
  }
}