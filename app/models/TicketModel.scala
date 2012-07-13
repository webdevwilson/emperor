package models

import anorm._
import anorm.SqlParser._
import chc._
import java.util.Date
import play.api.db.DB
import play.api.Play.current
import scala.collection.mutable.ListBuffer

case class InitialComment(
  comment: String
)

case class Comment(
  id: Pk[Long] = NotAssigned, userId: Long, username: String,
  realName: String, ticketId: String, content: String, dateCreated: Date
)

case class StatusChange(
  statusId: Long, comment: Option[String]
)

case class Resolution(
  resolutionId: Long, comment: Option[String]
)

case class InitialTicket(
  reporterId: Long, assigneeId: Option[Long] = None, projectId: Long,
  priorityId: Long, severityId: Long, typeId: Long, position: Option[Long] = None,
  summary: String, description: Option[String] = None
)

case class EditTicket(
  ticketId: Pk[String] = NotAssigned, reporterId: Long, assigneeId: Option[Long],
  attentionId: Option[Long], projectId: Long,
  priorityId: Long, resolutionId: Option[Long],
  proposedResolutionId: Option[Long], severityId: Long,
  typeId: Long, position: Option[Long], summary: String,
  description: Option[String]
)

case class FullTicket(
  id: Pk[Long] = NotAssigned, ticketId: String, user: TicketForThing, reporter: TicketForThing,
  assignee: TicketForOptThing, attention: TicketForOptThing,
  project: TicketForThing,  priority: TicketForColoredThing,
  resolution: TicketForOptThing,
  proposedResolution: TicketForOptThing,
  severity: TicketForColoredThing, workflowStatusId: Long, status: TicketForThing,
  ttype: TicketForColoredThing, position: Option[Long],
  summary: String, description: Option[String], dateCreated: Date
)

case class Ticket(
  id: Pk[Long] = NotAssigned, ticketId: String, reporterId: Long, assigneeId: Long,
  attentionId: Long, projectId: Long, priorityId: Long,
  resolutionId: Option[Long], proposedResolutionId: Option[Long],
  severityId: Long, statusId: Long, typeId: Long, position: Option[Long],
  summary: String, description: Option[String], dateCreated: Date
)

case class TicketForThing(
  id: Long, name: String
)

case class TicketForColoredThing(
  id: Long, name: String, color: String
)

case class TicketForOptThing(
  id: Option[Long], name: Option[String]
)

case class TicketHistory(
  id: Pk[Long] = NotAssigned, userId: Long, ticketId: Long,
  dateOccurred: Date
)

case class TicketFullHistory(
  id: Pk[Long] = NotAssigned, userId: Long, ticketId: Long,
  oldTicket: FullTicket, newTicket: FullTicket, dateOccurred: Date
)

object TicketModel {

  val allCommentsQuery = SQL("SELECT * FROM ticket_comments JOIN users ON users.id = ticket_comments.user_id")
  val allQuery = SQL("SELECT * FROM tickets")
  val getByIdQuery = SQL("SELECT * FROM full_tickets WHERE ticket_id={ticket_id}")
  val getAllCurrentQuery = SQL("SELECT * FROM full_tickets ORDER BY date_created DESC")
  val getFullByIdQuery = SQL("SELECT * FROM full_tickets WHERE ticket_id={ticket_id}")
  val getAllFullByIdQuery = SQL("SELECT * FROM full_all_tickets t  WHERE t.ticket_id={ticket_id} ORDER BY date_created ASC")
  val getAllFullByIdCountQuery = SQL("SELECT COUNT(*) FROM full_all_tickets  WHERE ticket_id={ticket_id} ORDER BY date_created ASC")
  val listQuery = SQL("SELECT * FROM tickets LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM tickets")
  val insertQuery = SQL("INSERT INTO tickets (ticket_id, user_id, reporter_id, assignee_id, project_id, priority_id, severity_id, status_id, type_id, position, summary, description, date_created) VALUES ({ticket_id}, {user_id}, {reporter_id}, {assignee_id}, {project_id}, {priority_id}, {severity_id}, {status_id}, {type_id}, {position}, {summary}, {description}, UTC_TIMESTAMP())")
  val updateQuery = SQL("INSERT INTO tickets (ticket_id, user_id, project_id, reporter_id, assignee_id, attention_id, priority_id, severity_id, status_id, type_id, resolution_id, proposed_resolution_id, position, summary, description, date_created) VALUES ({ticket_id}, {user_id}, {project_id}, {reporter_id}, {assignee_id}, {attention_id}, {priority_id}, {severity_id}, {status_id}, {type_id}, {resolution_id}, {proposed_resolution_id}, {position}, {summary}, {description}, UTC_TIMESTAMP())")
  val getCommentByIdQuery = SQL("SELECT * FROM ticket_comments JOIN users ON users.id = ticket_comments.user_id WHERE ticket_comments.id={id} ORDER BY ticket_comments.date_created")
  val insertCommentQuery = SQL("INSERT INTO ticket_comments (user_id, ticket_id, content, date_created) VALUES ({user_id}, {ticket_id}, {content}, UTC_TIMESTAMP())")
  val deleteCommentQuery = SQL("DELETE FROM ticket_comments WHERE id={id}")
  val deleteQuery = SQL("DELETE FROM tickets WHERE id={id}")

  val getByProjectQuery = SQL("SELECT * FROM tickets WHERE project_id={project_id}")
  val getCountByProjectQuery = SQL("SELECT COUNT(*) FROM tickets WHERE project_id={project_id}")

  val getOpenByProjectQuery = SQL("SELECT * FROM tickets WHERE project_id={project_id} AND resolution_id IS NULL")
  val getCountOpenByProjectQuery = SQL("SELECT COUNT(*) FROM tickets WHERE project_id={project_id} AND resolution_id IS NULL")

  val getByProjectAndStatusQuery = SQL("SELECT * FROM tickets WHERE project_id={project_id} AND status_id={status_id}")
  val getCountByProjectAndStatusQuery = SQL("SELECT COUNT(*) FROM tickets WHERE project_id={project_id} AND status_id={status_id}")

  val getCountTodayByProjectQuery = SQL("SELECT COUNT(*) FROM tickets WHERE project_id={project_id} AND DATE(date_created) = DATE(NOW())")
  val getCountThisWeekByProjectQuery = SQL("SELECT COUNT(*) FROM tickets WHERE project_id={project_id} AND DATE(date_created) + 7 > DATE(NOW()) ")

  val ticket = {
    get[Pk[Long]]("id") ~
    get[String]("ticket_id") ~
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
      case id~tickId~repId~assId~attId~projId~priId~resId~propResId~sevId~statId~typeId~position~summary~description~dateCreated => Ticket(
        id = id,
        ticketId = tickId,
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
    get[Pk[String]]("ticket_id") ~
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
        ticketId = id,
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
    get[Pk[Long]]("id") ~
    get[String]("ticket_id") ~
    get[Long]("user_id") ~
    get[String]("user_realname") ~
    get[Long]("reporter_id") ~
    get[String]("reporter_realname") ~
    get[Option[Long]]("assignee_id") ~
    get[Option[String]]("assignee_realname") ~
    get[Option[Long]]("attention_id") ~
    get[Option[String]]("attention_realname") ~
    get[Long]("project_id") ~
    get[String]("project_name") ~
    get[Long]("priority_id") ~
    get[String]("priority_name") ~
    get[String]("priority_color") ~
    get[Option[Long]]("resolution_id") ~
    get[Option[String]]("resolution_name") ~
    get[Option[Long]]("proposed_resolution_id") ~
    get[Option[String]]("proposed_resolution_name") ~
    get[Long]("severity_id") ~
    get[String]("severity_name") ~
    get[String]("severity_color") ~
    get[Long]("status_id") ~
    get[Long]("workflow_status_id") ~
    get[String]("status_name") ~
    get[Long]("type_id") ~
    get[String]("type_name") ~
    get[String]("type_color") ~
    get[Option[Long]]("position") ~
    get[String]("summary") ~
    get[Option[String]]("description") ~
    get[Date]("date_created") map {
      case id~tickId~userId~userName~repId~repName~assId~assName~attId~attName~projId~projName~priId~priName~priColor~resId~resName~propResId~propResName~sevId~sevName~sevColor~statusId~workflowStatusId~statusName~typeId~typeName~typeColor~position~summary~description~dateCreated =>
        FullTicket(
          id = id,
          ticketId = tickId,
          user = TicketForThing(userId, userName),
          reporter = TicketForThing(repId, repName),
          assignee = TicketForOptThing(assId, assName),
          attention = TicketForOptThing(attId, attName),
          project = TicketForThing(projId, projName),
          priority = TicketForColoredThing(priId, priName, priColor),
          resolution = TicketForOptThing(resId, resName),
          proposedResolution = TicketForOptThing(propResId, propResName),
          severity = TicketForColoredThing(sevId, sevName, sevColor),
          workflowStatusId = workflowStatusId,
          status = TicketForThing(statusId, statusName),
          ttype = TicketForColoredThing(typeId, typeName, typeColor),
          position = position,
          summary = summary,
          description = description,
          dateCreated = dateCreated
        )
    }
  }

  val comment = {
    get[Pk[Long]]("ticket_comments.id") ~
    get[Long]("user_id") ~
    get[String]("username") ~
    get[String]("realname") ~
    get[String]("ticket_id") ~
    get[String]("content") ~
    get[Date]("ticket_comments.date_created") map {
      case id~userId~username~realName~ticketId~content~dateCreated => Comment(id, userId, username, realName, ticketId, content, dateCreated)
    }
  }

  /**
   * Add a comment.
   */
  def addComment(ticketId: String, userId: Long, content: String) : Option[Comment] = {

    val ticket = this.getById(ticketId)

    ticket match {
      case Some(ticket) => {
        DB.withConnection { implicit conn =>
          val id = insertCommentQuery.on(
            'user_id    -> userId,
            'ticket_id  -> ticketId,
            'content    -> content
          ).executeInsert()
          getCommentById(id.get)
        }
      }
      case None => return None
    }
  }

  /**
   * Delete comment.
   */
  def deleteComment(id: Long) = {
    DB.withConnection { implicit conn =>
      deleteCommentQuery.on('id -> id).execute
    }
  }

  def resolve(ticketId: String, userId: Long, resolutionId: Long, comment: Option[String] = None) = {

    DB.withConnection { implicit conn =>

      val tick = this.getById(ticketId).get
      this.update(userId = userId, id = ticketId, ticket = tick, resolutionId = Some(resolutionId), comment = comment)
    }
  }

  def unresolve(ticketId: String, userId: Long, comment: Option[String] = None) = {
      val tick = this.getById(ticketId).get

      this.update(userId = userId, id = ticketId, ticket = tick, resolutionId = None, clearResolution = true, comment = comment)
  }

  def changeStatus(ticketId: String, newStatusId: Long, userId: Long, comment: Option[String] = None) = {

    DB.withConnection { implicit conn =>

      val tick = this.getById(ticketId).get

      this.update(userId = userId, id = ticketId, ticket = tick, statusId = Some(newStatusId), comment = comment)
    }
  }

  /**
   * Create a ticket.
   */
  def create(userId: Long, ticket: InitialTicket): Option[FullTicket] = {

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

          // If these are missing? XXX
          val proj = ProjectModel.getById(ticket.projectId).get
          val tid = ProjectModel.getNextSequence(ticket.projectId).get

          val ticketId = proj.key + "-" + tid.toString
          val id = insertQuery.on(
            'ticket_id    -> ticketId,
            'user_id      -> userId,
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
          this.getFullById(ticketId)
        }
      }
      case None => None
    }

    result
  }

  /**
   * Delete a ticket.
   */
  def delete(id: Long) {
    DB.withConnection { implicit conn =>
      deleteQuery.on('id -> id).execute
    }
  }

  /**
   * Get a comment by id.
   */
  def getCommentById(id: Long) : Option[Comment] = {

    DB.withConnection { implicit conn =>
      getCommentByIdQuery.on('id -> id).as(comment.singleOpt)
    }
  }

  /**
   * Get ticket by ticketId.
   */
  def getById(id: String) : Option[EditTicket] = {

    DB.withConnection { implicit conn =>
      getByIdQuery.on('ticket_id -> id).as(editTicket.singleOpt)
    }
  }

  /**
   * Get ticket by ticketId.  This version returns the `FullTicket`.
   */
  def getFullById(id: String) : Option[FullTicket] = {

    DB.withConnection { implicit conn =>
      getFullByIdQuery.on('ticket_id -> id).as(fullTicket.singleOpt)
    }
  }

  def getAllCurrent: List[Ticket] = {

    DB.withConnection { implicit conn =>
      allQuery.as(ticket *)
    }
  }

  def getAllCurrentFull: List[FullTicket] = {
    DB.withConnection { implicit conn =>
      getAllCurrentQuery.as(fullTicket *)
    }
  }

  def getAllComments: List[Comment] = {

    DB.withConnection { implicit conn =>
      allCommentsQuery.as(comment *)
    }
  }

  def getAllFullById(id: String): List[FullTicket] = {

    DB.withConnection { implicit conn =>
      getAllFullByIdQuery.on('ticket_id -> id).as(fullTicket *)
    }
  }

  def getAllFullCountById(id: String): Long = {

    DB.withConnection { implicit conn =>
      getAllFullByIdCountQuery.on('ticket_id -> id).as(scalar[Long].single)
    }
  }

  def getCountByProject(projectId: Pk[Long]): Long = {

    DB.withConnection { implicit conn =>
      getCountByProjectQuery.on('project_id -> projectId).as(scalar[Long].single)
    }
  }

  def getCountOpenByProject(projectId: Pk[Long]): Long = {

    DB.withConnection { implicit conn =>
      getCountOpenByProjectQuery.on('project_id -> projectId).as(scalar[Long].single)
    }
  }

  def getCountByProjectAndStatus(projectId: Pk[Long], statusId: Long): Long = {

    DB.withConnection { implicit conn =>
      getByProjectAndStatusQuery.on('project_id -> projectId, 'status_id -> statusId).as(scalar[Long].single)
    }
  }

  def getCountTodayByProject(projectId: Pk[Long]): Long = {

    DB.withConnection { implicit conn =>
      getCountTodayByProjectQuery.on('project_id -> projectId).as(scalar[Long].single)
    }
  }

  def getCountThisWeekByProject(projectId: Pk[Long]): Long = {

    DB.withConnection { implicit conn =>
      getCountThisWeekByProjectQuery.on('project_id -> projectId).as(scalar[Long].single)
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

  def update(
    userId: Long, id: String, ticket: EditTicket,
    resolutionId: Option[Long] = None, statusId: Option[Long] = None,
    clearResolution: Boolean = false,
    comment: Option[String] = None
  ) = {

    val user = UserModel.getById(userId).get

    val oldTicket = DB.withConnection { implicit conn =>
      this.getFullById(id).get
    }

    // This is a bit hinky, so some explanation is required.
    // We could get passed a new resolutionId.  If so then we are changing
    // the resolution.  But if we DON'T get one (None) then we could either
    // be leaving the resolution alone OR setting it to None.  To disambiguate
    // we use the clearResolution boolean.  If that is true then we will
    // set newResId to None (regladless of what resolutionId we might've gotten).
    val newResId = clearResolution match {
      case true   => None
      case false  => resolutionId.getOrElse(oldTicket.resolution.id)
    }

    var changed = false
    if(oldTicket.project.id != ticket.projectId) {
      changed = true
    }
    if(!changed && (oldTicket.priority.id != ticket.priorityId)) {
      changed = true
    }
    if(!changed && (oldTicket.resolution.id != newResId)) {
      changed = true
    }
    if(!changed && (oldTicket.proposedResolution.id != ticket.proposedResolutionId)) {
      changed = true
    }
    if(!changed && (oldTicket.reporter.id != ticket.reporterId)) {
      changed = true
    }
    if(!changed && (oldTicket.assignee.id != ticket.assigneeId)) {
      changed = true
    }
    if(!changed && (oldTicket.attention.id != ticket.attentionId)) {
      changed = true
    }
    if(!changed && (oldTicket.severity.id != ticket.severityId)) {
      changed = true
    }
    if(!changed && (oldTicket.status.id != statusId.getOrElse(oldTicket.status.id))) {
      changed = true
    }
    if(!changed && (oldTicket.ttype.id != ticket.typeId)) {
      changed = true
    }
    if(!changed && (oldTicket.description != ticket.description)) {
      changed = true
    }
    if(!changed && (oldTicket.summary != ticket.summary)) {
      changed = true
    }

    if(changed) {
      // Only record something if a change was made.
      val tid = DB.withTransaction { implicit conn =>

        // XXX Project
        updateQuery.on(
          'ticket_id              -> id,
          'user_id                -> userId,
          'project_id             -> ticket.projectId,
          'reporter_id            -> ticket.reporterId,
          'assignee_id            -> ticket.assigneeId,
          'attention_id           -> ticket.attentionId,
          'priority_id            -> ticket.priorityId,
          'severity_id            -> ticket.severityId,
          'status_id              -> statusId.getOrElse(oldTicket.status.id),
          'type_id                -> ticket.typeId,
          'resolution_id          -> newResId,
          'proposed_resolution_id -> ticket.proposedResolutionId,
          'position               -> ticket.position,
          'description            -> ticket.description,
          'summary                -> ticket.summary
        ).executeInsert()

        // Add a comment, if we had one.
        comment.map { content => {
          val comm = addComment(ticketId = id, userId = userId, content = content)
          SearchModel.indexComment(comm.get)
        } }
      }

      val newTicket = DB.withConnection { implicit conn =>
        getFullById(id).get
      }

      if(changed) {
        SearchModel.indexHistory(newTick = newTicket, oldTick = oldTicket)
      }
    }
  }
}
