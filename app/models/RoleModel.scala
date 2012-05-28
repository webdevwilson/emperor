package models

import anorm._
import anorm.SqlParser._
import chc._
import java.util.Date
import play.api.db.DB
import play.api.Play.current
import play.Logger

case class Role(id: Pk[Long] = NotAssigned, name: String, description: Option[String], dateCreated: Date)

object RoleModel {

  val allQuery = SQL("SELECT * FROM roles")
  val getByIdQuery = SQL("SELECT * FROM roles WHERE id={id}")
  val listQuery = SQL("SELECT * FROM roles LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM roles")
  val addQuery = SQL("INSERT INTO roles (name, description, date_created) VALUES ({name}, {description}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE roles SET name={name}, description={description} WHERE id={id}")
  val lastInsertQuery = SQL("SELECT LAST_INSERT_ID()")

  val role = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[Option[String]]("description") ~
    get[Date]("date_created") map {
      case id~name~description~dateCreated => Role(id, name, description, dateCreated)
    }
  }
  
  def create(role: Role): Role = {

    DB.withConnection { implicit conn =>
      addQuery.on(
        'name       -> role.name,
        'description-> role.description
      ).executeUpdate

      val id = lastInsertQuery.as(scalar[Long].single)

      role.copy(id = new Id(id))
    }
  }
  
  def delete(id: Long) {
      // XXX
  }

  def getById(id: Long) : Option[Role] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(role.singleOpt)
    }
  }

  def getAll: List[Role] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(role *)
    }
  }

  def list(page: Int = 0, count: Int = 10) : Page[Role] = {

      val offset = count * page
      
      DB.withConnection { implicit conn =>
        val roles = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(role *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(roles, page, count, totalRows)
      }
  }
  
  def update(id: Long, role: Role) = {

    DB.withTransaction { implicit conn =>
      val foo = updateQuery.on(
        'id         -> id,
        'name       -> role.name,
        'description-> role.description
      ).executeUpdate
    }
  }
}