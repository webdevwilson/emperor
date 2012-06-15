package models

import anorm._
import anorm.SqlParser._
import chc._
import java.util.Date
import play.api.db.DB
import play.api.Play.current

case class TicketStatus(id: Pk[Long] = NotAssigned, name: String, dateCreated: Date)

object TicketStatusModel {

  val allQuery = SQL("SELECT * FROM ticket_statuses")
  val getByIdQuery = SQL("SELECT * FROM ticket_statuses WHERE id={id}")
  val listQuery = SQL("SELECT * FROM ticket_statuses LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM ticket_statuses")
  val insertQuery = SQL("INSERT INTO ticket_statuses (name, date_created) VALUES ({name}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE ticket_statuses SET name={name} WHERE id={id}")

  val ticket_status = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[Date]("date_created") map {
      case id~name~dateCreated => TicketStatus(id, name, dateCreated)
    }
  }

  def create(ts: TicketStatus): TicketStatus = {

    DB.withConnection { implicit conn =>
      val id = insertQuery.on(
        'name   -> ts.name
      ).executeInsert()

      this.getById(id.get).get
    }
  }
  
  def delete(id: Long) {
      
  }

  def getById(id: Long) : Option[TicketStatus] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(ticket_status.singleOpt)
    }
  }

  def getAll: List[TicketStatus] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(ticket_status *)
    }
  }

  def list(page: Int = 0, count: Int = 10) : Page[TicketStatus] = {

      val offset = count * page
      
      DB.withConnection { implicit conn =>
        val tss = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(ticket_status *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(tss, page, count, totalRows)
      }
  }
  
  def update(id: Long, ts: TicketStatus) = {

    DB.withTransaction { implicit conn =>
      val foo = updateQuery.on(
        'id         -> id,
        'name       -> ts.name
      ).executeUpdate
    }
  }
}