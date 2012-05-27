package models

import anorm._
import anorm.SqlParser._
import chc._
import play.api.db.DB
import play.api.Play.current

case class InitialComment(
  content: String
)

case class Comment(
  id: Pk[Long] = NotAssigned, userId: Long, ticketId: Long, content: String
)

case class StatusChange(
  statusId: Long, comment: Option[String]
)

case class InitialTicket(
  userId: Long, projectId: Long, priorityId: Long, severityId: Long,
  typeId: Long, position: Option[Long], summary: String,
  description: Option[String]
)

case class Ticket(
  id: Pk[Long] = NotAssigned, reporterId: Long, projectId: Long,
  priorityId: Long, resolutionId: Option[Long],
  proposedResolutionId: Option[Long], severityId: Long, statusId: Long,
  typeId: Long, position: Option[Long], summary: String,
  description: Option[String]
)

case class FullTicket(
  id: Pk[Long] = NotAssigned, reportId: Long, projectId: Long,
  projectName: String,  priorityId: Long, priorityName: String,
  resolutionId: Option[Long],  proposedResolutionId: Option[Long],
  severityId: Long, severityName: String, workflowStatusId: Long,
  statusId: Long, statusName: String, typeId: Long, typeName: String,
  position: Option[Long], summary: String, description: Option[String]
)

object TicketModel {

  val allQuery = SQL("SELECT * FROM tickets")
  val getByIdQuery = SQL("SELECT * FROM tickets WHERE id={id}")
  val getFullByIdQuery = SQL("SELECT * FROM tickets t JOIN projects p ON p.id = t.project_id JOIN ticket_priorities tp ON tp.id = t.priority_id JOIN ticket_severities sevs ON sevs.id = t.severity_id JOIN workflow_statuses ws ON ws.id = t.status_id JOIN ticket_statuses ts ON ts.id = ws.status_id JOIN ticket_types tt ON tt.id = t.type_id WHERE t.id={id}")
  val listQuery = SQL("SELECT * FROM tickets LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM tickets")
  val insertQuery = SQL("INSERT INTO tickets (reporter_id, project_id, priority_id, severity_id, status_id, type_id, position, summary, description) VALUES ({reporter_id}, {project_id}, {priority_id}, {severity_id}, {status_id}, {type_id}, {position}, {summary}, {description})")
  val updateQuery = SQL("UPDATE tickets SET reporter_id={reporter_id}, project_id={project_id}, priority_id={priority_id}, resolution_id={resolution_id}, severity_id={severity_id}, status_id={status_id}, type_id={type_id}, position={position}, summary={summary}, description={description} WHERE id={id}")
  val updateStatusQuery = SQL("UPDATE tickets SET status_id={status_id} WHERE id={ticket_id}")
  val lastInsertQuery = SQL("SELECT LAST_INSERT_ID()")
  val insertCommentQuery = SQL("INSERT INTO ticket_comments (user_id, ticket_id, content) VALUES ({user_id}, {ticket_id}, {content})")
  val getOpenCountForProjectQuery = SQL("SELECT count(*) FROM tickets WHERE resolution_id IS NULL and proposed_resolution_id IS NULL AND project_id={project_id}")
  val getCommentsQuery = SQL("SELECT * FROM ticket_comments WHERE ticket_id={ticket_id}")
  val getCommentsCountQuery = SQL("SELECT count(*) FROM ticket_comments WHERE ticket_id={ticket_id}")
  val insertHistoryQuery = SQL("INSERT INTO ticket_history SELECT t.*, {user_id} FROM tickets t WHERE t.id={ticket_id}")

  val ticket = {
    get[Pk[Long]]("id") ~
    get[Long]("reporter_id") ~
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
      case id~reporterId~projectId~priorityId~resolutionId~proposedResolutionId~severityId~statusId~typeId~position~summary~description => Ticket(
        id, reporterId, projectId, priorityId, resolutionId, proposedResolutionId, severityId, statusId, typeId, position, summary, description
      )
    }
  }

  val fullTicket = {
    get[Pk[Long]]("tickets.id") ~
    get[Long]("tickets.reporter_id") ~
    get[Long]("tickets.project_id") ~
    get[String]("projects.name") ~
    get[Long]("tickets.priority_id") ~
    get[String]("ticket_priorities.name") ~
    get[Option[Long]]("tickets.resolution_id") ~
    get[Option[Long]]("tickets.proposed_resolution_id") ~
    get[Long]("tickets.severity_id") ~
    get[String]("ticket_severities.name") ~
    get[Long]("tickets.status_id") ~
    get[Long]("workflow_statuses.status_id") ~
    get[String]("ticket_statuses.name") ~
    get[Long]("tickets.type_id") ~
    get[String]("ticket_types.name") ~
    get[Option[Long]]("tickets.position") ~
    get[String]("tickets.summary") ~
    get[Option[String]]("tickets.description") map {
      case id~reporterId~projectId~projectName~priorityId~priorityName~resolutionId~proposedResolutionId~severityId~severityName~workflowStatusId~statusId~statusName~typeId~typeName~position~summary~description => FullTicket(
        id, reporterId, projectId, projectName, priorityId, priorityName, resolutionId, proposedResolutionId, severityId, severityName, workflowStatusId, statusId, statusName, typeId, typeName, position, summary, description
      )
    }
  }

  val comment = {
    get[Pk[Long]]("id") ~
    get[Long]("user_id") ~
    get[Long]("ticket_id") ~
    get[String]("content") map {
      case id~userId~ticketId~content => Comment(id, userId, ticketId, content)
    }
  }

  def addComment(ticketId: Long, userId: Long, content: String) : Boolean = {
    
    val ticket = this.findById(ticketId)
    
    ticket match {
      case Some(ticket) => {
        DB.withConnection { implicit conn =>
          insertCommentQuery.on(
            'user_id    -> userId,
            'ticket_id  -> ticketId,
            'content    -> content
          ).execute
        }
      }
      case None => return false
    }

    true
  }

  def advance(ticketId: Long, statusId: Long) = {
    
    DB.withConnection { implicit conn =>
      updateStatusQuery.on(
        'status_id  -> statusId,
        'ticket_id  -> ticketId
      ).execute
      // XXX history! need userId!
    }
  }

  def create(ticket: InitialTicket): Option[Ticket] = {

    val project = ProjectModel.findById(ticket.projectId)
    
    // Fetch the starting status we should use for the project's workflow.
    val startingStatus = project match {
      case Some(x) => WorkflowModel.getStartingStatus(x.workflowId)
      case None => None
    }
    // XXX Should log something up there, really

    val result = startingStatus match {
      case Some(status) => {
        DB.withConnection { implicit conn =>
          insertQuery.on(
            'reporter_id  -> ticket.userId,
            'project_id   -> ticket.projectId,
            'priority_id  -> ticket.priorityId,
            'severity_id  -> ticket.severityId,
            'status_id    -> status.id,
            'type_id      -> ticket.typeId,
            'description  -> ticket.description,
            'position     -> ticket.position,
            'summary      -> ticket.summary
          ).execute
          val id = lastInsertQuery.as(scalar[Long].single)
          this.findById(id)
        }
      }
      case None => None
    }

    result
  }
  
  def delete(id: Long) {
      
  }

  def findById(id: Long) : Option[Ticket] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(ticket.singleOpt)
    }
  }

  def findFullById(id: Long) : Option[FullTicket] = {
      
    DB.withConnection { implicit conn =>
      getFullByIdQuery.on('id -> id).as(fullTicket.singleOpt)
    }
  }
  
  def getAll: List[Ticket] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(ticket *)
    }
  }

  def getComments(ticketId: Long, page: Int = 0, count: Int = 10) : Page[Comment] = {
    
    val offset = count * page
    
    DB.withConnection { implicit conn =>
      val comments = getCommentsQuery.on('ticket_id -> ticketId).as(comment *)
      
      val totalRows = getCommentsCountQuery.on('ticket_id -> ticketId).as(scalar[Long].single)
      
      Page(comments, page, count, totalRows)
    }
  }

  def getOpenCountForProject(projectId: Long) : Long = {
    
    DB.withConnection { implicit conn =>
      getOpenCountForProjectQuery.on('project_id -> projectId).as(scalar[Long].single)
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
  
  def update(userId: Long, id: Long, ticket: Ticket) = {

    DB.withTransaction { implicit conn =>

      insertHistoryQuery.on(
        'user_id -> userId,
        'ticket_id -> id
      ).executeUpdate

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