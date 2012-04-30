package models

import anorm._
import anorm.SqlParser._
import chc._
import play.api.db.DB
import play.api.Play.current
import play.Logger

case class Group(id: Pk[Long] = NotAssigned, name: String)

object GroupModel {

  val addUserToGroupQuery = SQL("INSERT IGNORE INTO user_groups (user_id, group_id) VALUES ({userId}, {groupId})")
  val allQuery = SQL("SELECT * FROM groups")
  val findStartsWithQuery("SELECT * FROM groups WHERE name={name}")
  val getByIdQuery = SQL("SELECT * FROM groups WHERE id={id}")
  val listQuery = SQL("SELECT * FROM groups LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM groups")
  val addQuery = SQL("INSERT INTO groups (name) VALUES ({name})")
  val updateQuery = SQL("UPDATE groups SET name={name} WHERE id={id}")
  val lastInsertQuery = SQL("SELECT LAST_INSERT_ID()")

  val group = {
    get[Pk[Long]]("id") ~
    get[String]("name") map {
      case id~name => Group(id, name)
    }
  }

  def addUserToGroup(userId: Long, groupId: Long) {
    
    DB.withConnection { implicit conn =>
      addUserToGroupQuery.on(
        'userId -> userId,
        'groupId-> groupId
      )
    }
  }

  def create(group: Group): Group = {

    DB.withConnection { implicit conn =>
      addQuery.on(
        'name   -> group.name
      ).executeUpdate

      val id = lastInsertQuery.as(scalar[Long].single)

      group.copy(id = new Id(id))
    }
  }
  
  def delete(id: Long) {
      
  }

  def findById(id: Long) : Option[Group] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(GroupModel.group.singleOpt)
    }
  }

  def getAll: List[Group] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(group *)
    }
  }
  
  def list(page: Int = 0, count: Int = 10) : Page[Group] = {

      val offset = count * page
      
      DB.withConnection { implicit conn =>
        val groups = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(group *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(groups, page, count, totalRows)
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