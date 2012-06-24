package models

import anorm._
import anorm.SqlParser._
import chc._
import java.util.Date
import play.api.db.DB
import play.api.Play.current
import scala.collection.mutable.ListBuffer

case class InitialComment(
  content: String
)

case class Comment(
  id: Pk[Long] = NotAssigned, userId: Long, username: String,
  realName: String, ticketId: Long, content: String, dateCreated: Date
)

case class StatusChange(
  statusId: Long, comment: Option[String]
)

case class Resolution(
  resolutionId: Long, comment: Option[String]
)

case class InitialTicket(
  reporterId: Long, assigneeId: Option[Long], projectId: Long,
  priorityId: Long, severityId: Long, typeId: Long, position: Option[Long],
  summary: String, description: Option[String]
)

case class EditTicket(
  id: Pk[Long] = NotAssigned, reporterId: Long, assigneeId: Option[Long],
  attentionId: Option[Long], projectId: Long,
  priorityId: Long, resolutionId: Option[Long],
  proposedResolutionId: Option[Long], severityId: Long,
  typeId: Long, position: Option[Long], summary: String,
  description: Option[String]
)

// XXX assignee and attention name
case class FullTicket(
  id: Pk[Long] = NotAssigned, reporter: TicketForThing,
  assigneeId: Option[Long], attentionId: Option[Long],
  project: TicketForThing,  priority: TicketForThing,
  resolution: TicketForOptThing,
  proposedResolutionId: Option[Long], // proposedResolutionName: Option[String],
  severity: TicketForThing, workflowStatusId: Long, status: TicketForThing,
  ttype: TicketForThing, position: Option[Long],
  summary: String, description: Option[String], dateCreated: Date
)

case class Ticket(
  id: Pk[Long] = NotAssigned, reporterId: Long, assigneeId: Long,
  attentionId: Long, projectId: Long, priorityId: Long,
  resolutionId: Option[Long], proposedResolutionId: Option[Long],
  severityId: Long, statusId: Long, typeId: Long, position: Option[Long],
  summary: String, description: Option[String], dateCreated: Date
)

case class TicketForThing(
  id: Long, name: String
)

case class TicketForOptThing(
  id: Option[Long], name: Option[String]
)

case class TicketHistory(
  id: Pk[Long] = NotAssigned, ticketId: Long, projectId: Long,
  user: TicketForThing, reporter: TicketForThing, priority: TicketForThing,
  resolution: TicketForOptThing, proposedResolution: TicketForOptThing,
  severity: TicketForThing,  status: TicketForThing, ttype: TicketForThing,
  position: Option[Long], summary: String, description: Option[String],
  dateOccurred: Date
)

object TicketModel {

  val allCommentsQuery = SQL("SELECT * FROM ticket_comments tc JOIN users u ON u.id = tc.user_id")
  val allQuery = SQL("SELECT * FROM tickets")
  val allFullQuery = SQL("SELECT * FROM tickets t JOIN projects p ON p.id = t.project_id JOIN ticket_priorities tp ON tp.id = t.priority_id JOIN ticket_severities sevs ON sevs.id = t.severity_id JOIN workflow_statuses ws ON ws.id = t.status_id JOIN ticket_statuses ts ON ts.id = ws.status_id JOIN ticket_types tt ON tt.id = t.type_id JOIN users u ON u.id = t.reporter_id LEFT JOIN ticket_resolutions tr ON tr.id = t.resolution_id")
  val getByIdQuery = SQL("SELECT * FROM tickets WHERE id={id}")
  // XX Missing proposed resolution :( due to lack of aliases
  val getFullByIdQuery = SQL("SELECT * FROM tickets t JOIN projects p ON p.id = t.project_id JOIN ticket_priorities tp ON tp.id = t.priority_id JOIN ticket_severities sevs ON sevs.id = t.severity_id JOIN workflow_statuses ws ON ws.id = t.status_id JOIN ticket_statuses ts ON ts.id = ws.status_id JOIN ticket_types tt ON tt.id = t.type_id JOIN users u ON u.id = t.reporter_id LEFT JOIN ticket_resolutions tr ON tr.id = t.resolution_id WHERE t.id={id}")
  val listQuery = SQL("SELECT * FROM tickets LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM tickets")
  val insertQuery = SQL("INSERT INTO tickets (reporter_id, assignee_id, project_id, priority_id, severity_id, status_id, type_id, position, summary, description, date_created) VALUES ({reporter_id}, {assignee_id}, {project_id}, {priority_id}, {severity_id}, {status_id}, {type_id}, {position}, {summary}, {description}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE tickets SET reporter_id={reporter_id}, assignee_id={assignee_id}, attention_id={attention_id}, priority_id={priority_id}, resolution_id={resolution_id}, severity_id={severity_id}, type_id={type_id}, status_id={status_id}, position={position}, summary={summary}, description={description} WHERE id={id}")
  val getCommentByIdQuery = SQL("SELECT * FROM ticket_comments tc JOIN users u ON u.id = tc.user_id WHERE tc.id={id}")
  val insertCommentQuery = SQL("INSERT INTO ticket_comments (user_id, ticket_id, content, date_created) VALUES ({user_id}, {ticket_id}, {content}, UTC_TIMESTAMP())")
  val getOpenCountForProjectQuery = SQL("SELECT count(*) FROM tickets WHERE resolution_id IS NULL and proposed_resolution_id IS NULL AND project_id={project_id}")
  val getOpenCountForTodayProjectQuery = SQL("SELECT count(*) FROM tickets WHERE resolution_id IS NULL and proposed_resolution_id IS NULL AND project_id={project_id} AND date_created >= UTC_DATE()")
  val getOpenCountForWeekProjectQuery = SQL("SELECT count(*) FROM tickets WHERE resolution_id IS NULL and proposed_resolution_id IS NULL AND project_id={project_id} AND date_created >= DATE_SUB(UTC_DATE(), INTERVAL 1 WEEK)")

  val insertHistoryQuery = SQL("INSERT INTO ticket_history (user_id, ticket_id, project_id, priority_id, resolution_id, proposed_resolution_id, reporter_id, severity_id, status_id, type_id, position, summary, description, date_occurred) VALUES ({user_id}, {ticket_id}, {project_id}, {priority_id}, {resolution_id}, {proposed_resolution_id}, {reporter_id}, {severity_id}, {status_id}, {type_id}, {position}, {summary}, {description}, UTC_TIMESTAMP())")

  val ticket = {
    get[Pk[Long]]("id") ~
    get[Long]("reporter_id") ~
    get[Long]("assignee_id") ~
    get[Long]("attention_id") ~
    get[Long]("project_id") ~
    get[Long]("priority_id") ~
    get[Option[Long]]("resolution_id") ~
    get[Option[Long]]("proposed_resolution_id") ~
    get[Long]("severity_id") ~
    get[Long]("status_id") ~
    get[Long]("type_id") ~
    get[Option[Long]]("position") ~
    get[String]("summary") ~
    get[Option[String]]("description") ~
    get[Date]("date_created") map {
      case id~repId~assId~attId~projId~priId~resId~propResId~sevId~statId~typeId~position~summary~description~dateCreated => Ticket(
        id = id,
        reporterId = repId,
        assigneeId = assId,
        attentionId = attId,
        projectId = projId,
        priorityId = priId,
        resolutionId = resId,
        proposedResolutionId = propResId,
        severityId = sevId,
        statusId = statId,
        typeId = typeId,
        position = position,
        summary = summary,
        description = description,
        dateCreated = dateCreated
      )
    }
  }

  val editTicket = {
    get[Pk[Long]]("id") ~
    get[Long]("reporter_id") ~
    get[Option[Long]]("assignee_id") ~
    get[Option[Long]]("attention_id") ~
    get[Long]("project_id") ~
    get[Long]("priority_id") ~
    get[Option[Long]]("resolution_id") ~
    get[Option[Long]]("proposed_resolution_id") ~
    get[Long]("severity_id") ~
    get[Long]("type_id") ~
    get[Option[Long]]("position") ~
    get[String]("summary") ~
    get[Option[String]]("description") map {
      case id~reporterId~assigneeId~attentionId~projectId~priorityId~resolutionId~proposedResolutionId~severityId~typeId~position~summary~description => EditTicket(
        id = id,
        reporterId = reporterId,
        assigneeId = assigneeId,
        attentionId = attentionId,
        projectId = projectId,
        priorityId = priorityId,
        resolutionId = resolutionId,
        proposedResolutionId = proposedResolutionId,
        severityId = severityId,
        typeId = typeId,
        position = position,
        summary = summary,
        description = description
      )
    }
  }

  val fullTicket = {
    get[Pk[Long]]("tickets.id") ~
    get[Long]("tickets.reporter_id") ~
    get[String]("users.realname") ~
    get[Option[Long]]("tickets.assignee_id") ~
    get[Option[Long]]("tickets.attention_id") ~
    get[Long]("tickets.project_id") ~
    get[String]("projects.name") ~
    get[Long]("tickets.priority_id") ~
    get[String]("ticket_priorities.name") ~
    get[Option[Long]]("tickets.resolution_id") ~
    get[Option[String]]("ticket_resolutions.name") ~
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
    get[Option[String]]("tickets.description") ~
    get[Date]("tickets.date_created") map {
      case id~repId~repName~assId~attId~projId~projName~priId~priName~resId~resName~propResId~sevId~sevName~statusId~workflowStatusId~statusName~typeId~typeName~position~summary~description~dateCreated =>
        FullTicket(
          id = id,
          reporter = TicketForThing(repId, repName),
          assigneeId = assId,
          attentionId = attId,
          project = TicketForThing(projId, projName),
          priority = TicketForThing(priId, priName),
          resolution = TicketForOptThing(resId, resName),
          proposedResolutionId = propResId,
          severity = TicketForThing(sevId, sevName),
          workflowStatusId = workflowStatusId,
          status = TicketForThing(statusId, statusName),
          ttype = TicketForThing(typeId, typeName),
          position = position,
          summary = summary,
          description = description,
          dateCreated = dateCreated
        )
    }
  }

  val history = {
    get[Pk[Long]]("ticket_history.id") ~
    get[Long]("ticket_history.ticket_id") ~
    get[Long]("ticket_history.user_id") ~
    get[String]("users.realname") ~
    get[Long]("ticket_history.reporter_id") ~
    get[String]("users.realname") ~ // XXX rep.name
    get[Long]("ticket_history.project_id") ~
    get[Long]("ticket_history.priority_id") ~
    get[String]("ticket_priorities.name") ~
    get[Option[Long]]("ticket_history.resolution_id") ~
    get[Option[String]]("ticket_resolutions.name") ~
    get[Option[Long]]("ticket_history.proposed_resolution_id") ~
    get[Option[String]]("ticket_resolutions.name") ~ // XXX tpr.name
    get[Long]("ticket_history.severity_id") ~
    get[String]("ticket_severities.name") ~
    get[Long]("ticket_history.status_id") ~
    get[String]("ticket_statuses.name") ~
    get[Long]("ticket_history.type_id") ~
    get[String]("ticket_types.name") ~
    get[Option[Long]]("ticket_history.position") ~
    get[String]("ticket_history.summary") ~
    get[Option[String]]("ticket_history.description") ~
    get[Date]("ticket_history.date_occurred") map {
      case id~ticketId~userId~realName~repId~repRealName~projectId~prioId~prioName~resId~resName~propResId~propResName~sevId~sevName~statId~statName~typeId~typeName~position~summary~description~dateOccurred => {
        
        TicketHistory(
          id = id,
          ticketId = ticketId,
          projectId = projectId, 
          user = TicketForThing(userId, realName),
          reporter = TicketForThing(repId, repRealName),
          priority = TicketForThing(prioId, prioName),
          resolution = TicketForOptThing(resId, resName),
          proposedResolution = TicketForOptThing(propResId, propResName),
          severity = TicketForThing(sevId, sevName),
          status = TicketForThing(statId, statName),
          ttype = TicketForThing(typeId, typeName),
          position = position,
          summary = summary,
          description = description,
          dateOccurred = dateOccurred
        )
      }
    }
  }

  val comment = {
    get[Pk[Long]]("ticket_comments.id") ~
    get[Long]("user_id") ~
    get[String]("username") ~
    get[String]("realname") ~
    get[Long]("ticket_id") ~
    get[String]("content") ~
    get[Date]("date_created") map {
      case id~userId~username~realName~ticketId~content~dateCreated => Comment(id, userId, username, realName, ticketId, content, dateCreated)
    }
  }

  def addComment(ticketId: Long, userId: Long, content: String) : Option[Comment] = {
    
    val ticket = this.getById(ticketId)
    
    ticket match {
      case Some(ticket) => {
        DB.withConnection { implicit conn =>
          val id = insertCommentQuery.on(
            'user_id    -> userId,
            'ticket_id  -> ticketId,
            'content    -> content
          ).executeInsert()
          this.getCommentById(id.get)
        }
      }
      case None => return None
    }
  }
  
  def resolve(ticketId: Long, userId: Long, resolutionId: Long) = {
    
    DB.withConnection { implicit conn =>
      
      val tick = this.getById(ticketId).get
      val newTick = tick.copy(resolutionId = Some(resolutionId))
      
      this.update(userId = userId, id = ticketId, ticket = newTick)
    }
  }
  
  def unresolve(ticketId: Long, userId: Long) = {
      val tick = this.getById(ticketId).get
      val newTick = tick.copy(resolutionId = None)
      
      this.update(userId = userId, id = ticketId, ticket = newTick)
  }

  def changeStatus(ticketId: Long, newStatusId: Long, userId: Long) = {
    
    DB.withConnection { implicit conn =>

      val tick = this.getById(ticketId).get
      
      this.update(userId = userId, id = ticketId, ticket = tick, statusId = Some(newStatusId))
    }
  }

  def create(ticket: InitialTicket): Option[EditTicket] = {

    val project = ProjectModel.getById(ticket.projectId)
    
    // Fetch the starting status we should use for the project's workflow.
    val startingStatus = project match {
      case Some(x) => WorkflowModel.getStartingStatus(x.workflowId)
      case None => None
    }
    // XXX Should log something up there, really

    val result = startingStatus match {
      case Some(status) => {
        DB.withConnection { implicit conn =>
          val id = insertQuery.on(
            'reporter_id  -> ticket.reporterId,
            'assignee_id  -> ticket.assigneeId,
            'project_id   -> ticket.projectId,
            'priority_id  -> ticket.priorityId,
            'severity_id  -> ticket.severityId,
            'status_id    -> status.id,
            'type_id      -> ticket.typeId,
            'description  -> ticket.description,
            'position     -> ticket.position,
            'summary      -> ticket.summary
          ).executeInsert()
          this.getById(id.get)
        }
      }
      case None => None
    }

    result
  }
  
  def delete(id: Long) {
      
  }

  def getCommentById(id: Long) : Option[Comment] = {
      
    DB.withConnection { implicit conn =>
      getCommentByIdQuery.on('id -> id).as(comment.singleOpt)
    }
  }

  def getById(id: Long) : Option[EditTicket] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(editTicket.singleOpt)
    }
  }

  def getFullById(id: Long) : Option[FullTicket] = {
      
    DB.withConnection { implicit conn =>
      getFullByIdQuery.on('id -> id).as(fullTicket.singleOpt)
    }
  }

  def getAll: List[Ticket] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(ticket *)
    }
  }

  def getAllComments: List[Comment] = {
      
    DB.withConnection { implicit conn =>
      allCommentsQuery.as(comment *)
    }
  }

  def getAllFull: List[FullTicket] = {
      
    DB.withConnection { implicit conn =>
      allFullQuery.as(fullTicket *)
    }
  }

  def getOpenCountForProject(projectId: Long) : Long = {
    
    DB.withConnection { implicit conn =>
      getOpenCountForProjectQuery.on('project_id -> projectId).as(scalar[Long].single)
    }
  }

  def getOpenCountTodayForProject(projectId: Long) : Long = {
    
    DB.withConnection { implicit conn =>
      getOpenCountForTodayProjectQuery.on('project_id -> projectId).as(scalar[Long].single)
    }
  }

  def getOpenCountWeekForProject(projectId: Long) : Long = {
    
    DB.withConnection { implicit conn =>
      getOpenCountForWeekProjectQuery.on('project_id -> projectId).as(scalar[Long].single)
    }
  }

  def list(page: Int = 1, count: Int = 10) : Page[Ticket] = {

      val offset = count * (page - 1)
      
      DB.withConnection { implicit conn =>
        val tickets = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(ticket *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(tickets, page, count, totalRows)
      }
  }
  
  def update(userId: Long, id: Long, ticket: EditTicket, statusId : Option[Long] = None) = {

    val user = UserModel.getById(userId).get

    val oldTicket = DB.withConnection { implicit conn =>
      this.getFullById(id).get
    }

    val cid = DB.withTransaction { implicit conn =>
      
      // XXX needs to NOT create an entry if there are no differences!
      val cid = insertHistoryQuery.on(
        'user_id      -> userId,
        'ticket_id    -> id,
        'project_id   -> oldTicket.project.id,
        'priority_id  -> oldTicket.priority.id,
        'resolution_id -> oldTicket.resolution.id,
        'proposed_resolution_id -> oldTicket.proposedResolutionId,
        'reporter_id  -> oldTicket.reporter.id,
        'assignee_id  -> oldTicket.assigneeId,
        'attention_id -> oldTicket.attentionId,
        'severity_id  -> oldTicket.severity.id,
        'status_id    -> oldTicket.status.id,
        'type_id      -> oldTicket.ttype.id,
        'description  -> oldTicket.description,
        'position     -> oldTicket.position,
        'summary      -> oldTicket.summary
      ).executeInsert()

      updateQuery.on(
        'id                     -> id,
        'reporter_id            -> ticket.reporterId,
        'assigne_id             -> ticket.assigneeId,
        'attention_id           -> ticket.attentionId,
        'priority_id            -> ticket.priorityId,
        'status_id              -> statusId.getOrElse(oldTicket.status.id),
        'resolution_id          -> ticket.resolutionId,
        'proposed_resolution_id -> ticket.proposedResolutionId,
        'severity_id            -> ticket.severityId,
        'type_id                -> ticket.typeId,
        'description            -> ticket.description,
        'position               -> ticket.position,
        'summary                -> ticket.summary
      ).executeUpdate

      cid
    }
    
    val newTicket = DB.withConnection { implicit conn =>

      this.getFullById(id).get      
    }
    SearchModel.indexHistory(cid.get, userId, user.realName, newTicket, oldTicket)
  }
}