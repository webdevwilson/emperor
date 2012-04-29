package models

import anorm._
import anorm.SqlParser._
import chc._
import play.api.db.DB
import play.api.Play.current

case class TicketType(id: Pk[Long] = NotAssigned, name: String)

object TicketTypeModel {

  val allQuery = SQL("SELECT * FROM ticket_types")
  val getByIdQuery = SQL("SELECT * FROM ticket_types WHERE id={id}")
  val listQuery = SQL("SELECT * FROM ticket_types LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM ticket_types")
  val insertQuery = SQL("INSERT INTO ticket_types (name) VALUES ({name})")
  val updateQuery = SQL("UPDATE ticket_types SET name={name} WHERE id={id}")

  val ticket_type = {
    get[Pk[Long]]("id") ~
    get[String]("name") map {
      case id~name => TicketType(id, name)
    }
  }

  def create(ts: TicketType): TicketType = {

    DB.withConnection { implicit conn =>
      insertQuery.on(
        'name   -> ts.name
      ).executeUpdate
    }
    
    ts
  }
  
  def delete(id: Long) {
      
  }

  def findById(id: Long) : Option[TicketType] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(TicketTypeModel.ticket_type.singleOpt)
    }
  }

  def getAll: List[TicketType] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(ticket_type *)
    }
  }

  def list(page: Int = 0, count: Int = 10) : Page[TicketType] = {

      val offset = count * page
      
      DB.withConnection { implicit conn =>
        val tss = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(ticket_type *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(tss, page, count, totalRows)
      }
  }
  
  def update(id: Long, ts: TicketType) = {

    DB.withTransaction { implicit conn =>
      val foo = updateQuery.on(
        'id         -> id,
        'name       -> ts.name
      ).executeUpdate
    }
  }
}