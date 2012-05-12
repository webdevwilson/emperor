package models

import anorm._
import anorm.SqlParser._
import chc._
import play.api.db.DB
import play.api.Play.current

case class InitialTicket(
  projectId: Long, priorityId: Long, severityId: Long, typeId: Long,
  position: Option[Long], summary: String, description: Option[String]
)

case class Ticket(
  id: Pk[Long] = NotAssigned, projectId: Long, priorityId: Long,
  resolutionId: Option[Long], proposedResolutionId: Option[Long],
  severityId: Long, statusId: Long, typeId: Long, position: Option[Long],
  summary: String, description: Option[String]
)

object TicketModel {

  val allQuery = SQL("SELECT * FROM tickets")
  val getByIdQuery = SQL("SELECT * FROM tickets WHERE id={id}")
  val listQuery = SQL("SELECT * FROM tickets LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM tickets")
  val insertQuery = SQL("INSERT INTO tickets (project_id, priority_id, severity_id, status_id, type_id, position, summary, description) VALUES ({project_id}, {priority_id}, {severity_id}, {status_id}, {type_id}, {position}, {summary}, {description})")
  val updateQuery = SQL("UPDATE tickets SET project_id={project_id}, priority_id={priority_id}, resolution_id={resolution_id}, severity_id={severity_id}, status_id={status_id}, type_id={type_id}, position={position}, summary={summary}, description={description} WHERE id={id}")
  val lastInsertQuery = SQL("SELECT LAST_INSERT_ID()")

  val ticket = {
    get[Pk[Long]]("id") ~
    get[Long]("project_id") ~
    get[Long]("priority_id") ~
    get[Option[Long]]("resolution_id") ~
    get[Option[Long]]("proposed_resolution_id") ~
    get[Long]("severity_id") ~
    get[Long]("status_id") ~
    get[Long]("type_id") ~
    get[Option[Long]]("position") ~
    get[String]("summary") ~
    get[Option[String]]("description") map {
      case id~projectId~priorityId~resolutionId~proposedResolutionId~severityId~statusId~typeId~position~summary~description => Ticket(
        id, projectId, priorityId, resolutionId, proposedResolutionId, severityId, statusId, typeId, position, summary, description
      )
    }
  }

  def create(ticket: InitialTicket): Option[Ticket] = {

    DB.withConnection { implicit conn =>
      insertQuery.on(
        'project_id   -> ticket.projectId,
        'priority_id  -> ticket.priorityId,
        'severity_id  -> ticket.severityId,
        'status_id    -> 1, // XXX should not be hardcoded
        'type_id      -> ticket.typeId,
        'description  -> ticket.description,
        'position     -> ticket.position,
        'summary      -> ticket.summary
      ).execute

      val id = lastInsertQuery.as(scalar[Long].single)
      
      this.findById(id)
    }
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
        'id                     -> id,
        'priority_id            -> ticket.priorityId,
        'resolution_id          -> ticket.resolutionId,
        'proposed_resolution_id -> ticket.proposedResolutionId,
        'severity_id            -> ticket.severityId,
        'status_id              -> ticket.statusId,
        'type_id                -> ticket.typeId,
        'description            -> ticket.description,
        'position               -> ticket.position,
        'summary                -> ticket.summary
      ).executeUpdate
    }
  }
}