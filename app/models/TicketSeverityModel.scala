package models

import anorm._
import anorm.SqlParser._
import chc._
import java.util.Date
import play.api.db.DB
import play.api.Play.current

case class TicketSeverity(id: Pk[Long] = NotAssigned, name: String, position: Int, dateCreated: Date)

object TicketSeverityModel {

  val allQuery = SQL("SELECT * FROM ticket_severities ORDER BY position ASC")
  val getByIdQuery = SQL("SELECT * FROM ticket_severities WHERE id={id}")
  val listQuery = SQL("SELECT * FROM ticket_severities ORDER BY position ASC LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM ticket_severities")
  val insertQuery = SQL("INSERT INTO ticket_severities (name, position, date_created) VALUES ({name}, {position}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE ticket_severities SET name={name}, position={position} WHERE id={id}")
  val lastInsertQuery = SQL("SELECT LAST_INSERT_ID()")

  val ticket_severity = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[Int]("position") ~
    get[Date]("date_created") map {
      case id~name~position~dateCreated => TicketSeverity(id, name, position, dateCreated)
    }
  }

  def create(ts: TicketSeverity): Option[TicketSeverity] = {

    DB.withConnection { implicit conn =>
      insertQuery.on(
        'name     -> ts.name,
        'position -> ts.position
      ).executeUpdate

      val id = lastInsertQuery.as(scalar[Long].single)
      this.getById(id)
    }
    
  }
  
  def delete(id: Long) {
      
  }

  def getById(id: Long) : Option[TicketSeverity] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(ticket_severity.singleOpt)
    }
  }

  def getAll: List[TicketSeverity] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(ticket_severity *)
    }
  }

  def list(page: Int = 0, count: Int = 10) : Page[TicketSeverity] = {

      val offset = count * page
      
      DB.withConnection { implicit conn =>
        val tss = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(ticket_severity *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(tss, page, count, totalRows)
      }
  }
  
  def update(id: Long, ts: TicketSeverity) = {

    DB.withTransaction { implicit conn =>
      val foo = updateQuery.on(
        'id         -> id,
        'name       -> ts.name,
        'position   -> ts.position
      ).executeUpdate
    }
  }
}