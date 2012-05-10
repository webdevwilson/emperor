package models

import anorm._
import anorm.SqlParser._
import chc._
import play.api.db.DB
import play.api.Play.current

case class Ticket(
  id: Pk[Long] = NotAssigned, priorityId: Long, resolutionId: Option[Long],
  statusId: Long, severityId: Long, typeId: Long, position: Option[Long],
  summary: String, description: Option[String]
)

object TicketModel {

  val allQuery = SQL("SELECT * FROM tickets")
  val getByIdQuery = SQL("SELECT * FROM tickets WHERE id={id}")
  val listQuery = SQL("SELECT * FROM tickets LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM tickets")
  val insertQuery = SQL("INSERT INTO tickets (name) VALUES ({name})")
  val updateQuery = SQL("UPDATE tickets SET name={name} WHERE id={id}")

  val ticket = {
    get[Pk[Long]]("id") ~
    get[Long]("ticket_priority_id") ~
    get[Option[Long]]("ticket_resolution_id") ~
    get[Long]("ticket_status_id") ~
    get[Long]("ticket_severity_id") ~
    get[Long]("ticket_type_id") ~
    get[Option[Long]]("position") ~
    get[String]("summary") ~
    get[Option[String]]("description") map {
      case id~priorityId~resolutionId~severityId~statusId~typeId~position~summary~description => Ticket(
        id, priorityId, resolutionId, severityId, statusId, typeId, position, summary, description
      )
    }
  }

  def create(ticket: Ticket): Ticket = {

    DB.withConnection { implicit conn =>
      insertQuery.on(
        'ticket_priority_id -> ticket.priorityId,
        'ticket_severity_id -> ticket.severityId,
        'ticket_status_id   -> ticket.statusId,
        'ticket_type_id     -> ticket.typeId,
        'description        -> ticket.description,
        'position           -> ticket.position,
        'summary            -> ticket.summary
      ).executeUpdate
    }
    
    ticket
  }
  
  def delete(id: Long) {
      
  }

  def findById(id: Long) : Option[Ticket] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(TicketModel.ticket.singleOpt)
    }
  }

  def getAll: List[Ticket] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(ticket *)
    }
  }

  def list(page: Int = 0, count: Int = 10) : Page[Ticket] = {

      val offset = count * page
      
      DB.withConnection { implicit conn =>
        val tickets = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(ticket *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(tickets, page, count, totalRows)
      }
  }
  
  def update(id: Long, ticket: Ticket) = {

    DB.withTransaction { implicit conn =>
      val foo = updateQuery.on(
        'id         -> id,
        'ticket_priority_id   -> ticket.priorityId,
        'ticket_resolution_id -> ticket.resolutionId,
        'ticket_severity_id   -> ticket.severityId,
        'ticket_status_id     -> ticket.statusId,
        'ticket_type_id       -> ticket.typeId,
        'description          -> ticket.description,
        'position             -> ticket.position,
        'summary              -> ticket.summary
      ).executeUpdate
    }
  }
}