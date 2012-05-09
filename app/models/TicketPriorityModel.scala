package models

import anorm._
import anorm.SqlParser._
import chc._
import play.api.db.DB
import play.api.Play.current

case class TicketPriority(id: Pk[Long] = NotAssigned, name: String, position: Int)

object TicketPriorityModel {

  val allQuery = SQL("SELECT * FROM ticket_severities")
  val getByIdQuery = SQL("SELECT * FROM ticket_severities WHERE id={id}")
  val listQuery = SQL("SELECT * FROM ticket_severities LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM ticket_severities")
  val insertQuery = SQL("INSERT INTO ticket_severities (name, position) VALUES ({name}, {position})")
  val updateQuery = SQL("UPDATE ticket_severities SET name={name}, position={position} WHERE id={id}")

  val ticket_priority = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[Int]("position") map {
      case id~name~position => TicketPriority(id, name, position)
    }
  }

  def create(tp: TicketPriority): TicketPriority = {

    DB.withConnection { implicit conn =>
      insertQuery.on(
        'name     -> tp.name,
        'position -> tp.position
      ).executeUpdate
    }
    
    tp
  }
  
  def delete(id: Long) {
      
  }

  def findById(id: Long) : Option[TicketPriority] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(TicketPriorityModel.ticket_priority.singleOpt)
    }
  }

  def getAll: List[TicketPriority] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(ticket_priority *)
    }
  }

  def list(page: Int = 0, count: Int = 10) : Page[TicketPriority] = {

      val offset = count * page
      
      DB.withConnection { implicit conn =>
        val tps = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(ticket_priority *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(tps, page, count, totalRows)
      }
  }
  
  def update(id: Long, tp: TicketPriority) = {

    DB.withTransaction { implicit conn =>
      val foo = updateQuery.on(
        'id         -> id,
        'name       -> tp.name,
        'position   -> tp.position
      ).executeUpdate
    }
  }
}