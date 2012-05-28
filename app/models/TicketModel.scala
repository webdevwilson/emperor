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

case class InitialTicket(
  userId: Long, projectId: Long, priorityId: Long, severityId: Long,
  typeId: Long, position: Option[Long], summary: String,
  description: Option[String]
)

case class EditTicket(
  id: Pk[Long] = NotAssigned, reporterId: Long, projectId: Long,
  priorityId: Long, resolutionId: Option[Long],
  proposedResolutionId: Option[Long], severityId: Long,
  typeId: Long, position: Option[Long], summary: String,
  description: Option[String]
)

case class FullTicket(
  id: Pk[Long] = NotAssigned, reporterId: Long, projectId: Long,
  projectName: String,  priorityId: Long, priorityName: String,
  resolutionId: Option[Long],  proposedResolutionId: Option[Long],
  severityId: Long, severityName: String, workflowStatusId: Long,
  statusId: Long, statusName: String, typeId: Long, typeName: String,
  position: Option[Long], summary: String, description: Option[String],
  dateCreated: Date
)

case class Ticket(
  id: Pk[Long] = NotAssigned, reporterId: Long, projectId: Long,
  priorityId: Long, resolutionId: Option[Long],
  proposedResolutionId: Option[Long], severityId: Long, statusId: Long,
  typeId: Long, position: Option[Long], summary: String,
  description: Option[String], dateCreated: Date
)

case class TicketHistory(
  id: Pk[Long] = NotAssigned, ticketId: Long, userId: Long,
  reporterId: Long, projectId: Long, priorityId: Long,
  resolutionId: Option[Long],  proposedResolutionId: Option[Long],
  severityId: Long,  statusId: Long, typeId: Long,
  position: Option[Long], summary: String, description: Option[String],
  dateOccurred: Date
)

case class TicketChange(
  name: String, oldValue: String, newValue: String, dateOccurred: Date
)

object TicketModel {

  val allQuery = SQL("SELECT * FROM tickets")
  val getByIdQuery = SQL("SELECT * FROM tickets WHERE id={id}")
  val getFullByIdQuery = SQL("SELECT * FROM tickets t JOIN projects p ON p.id = t.project_id JOIN ticket_priorities tp ON tp.id = t.priority_id JOIN ticket_severities sevs ON sevs.id = t.severity_id JOIN workflow_statuses ws ON ws.id = t.status_id JOIN ticket_statuses ts ON ts.id = ws.status_id JOIN ticket_types tt ON tt.id = t.type_id WHERE t.id={id}")
  val listQuery = SQL("SELECT * FROM tickets LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM tickets")
  val insertQuery = SQL("INSERT INTO tickets (reporter_id, project_id, priority_id, severity_id, status_id, type_id, position, summary, description, date_created) VALUES ({reporter_id}, {project_id}, {priority_id}, {severity_id}, {status_id}, {type_id}, {position}, {summary}, {description}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE tickets SET reporter_id={reporter_id}, priority_id={priority_id}, resolution_id={resolution_id}, severity_id={severity_id}, type_id={type_id}, position={position}, summary={summary}, description={description} WHERE id={id}")
  val updateStatusQuery = SQL("UPDATE tickets SET status_id={status_id} WHERE id={ticket_id}")
  val lastInsertQuery = SQL("SELECT LAST_INSERT_ID()")
  val insertCommentQuery = SQL("INSERT INTO ticket_comments (user_id, ticket_id, content, date_created) VALUES ({user_id}, {ticket_id}, {content}, UTC_TIMESTAMP())")
  val getOpenCountForProjectQuery = SQL("SELECT count(*) FROM tickets WHERE resolution_id IS NULL and proposed_resolution_id IS NULL AND project_id={project_id}")
  val getOpenCountForTodayProjectQuery = SQL("SELECT count(*) FROM tickets WHERE resolution_id IS NULL and proposed_resolution_id IS NULL AND project_id={project_id} AND date_created >= UTC_DATE()")
  val getOpenCountForWeekProjectQuery = SQL("SELECT count(*) FROM tickets WHERE resolution_id IS NULL and proposed_resolution_id IS NULL AND project_id={project_id} AND date_created >= DATE_SUB(UTC_DATE(), INTERVAL 1 WEEK)")

  val getAllHistoryQuery = SQL("SELECT * FROM ticket_history th JOIN users u ON u.id = th.user_id WHERE ticket_id={ticket_id} ORDER BY th.date_created ASC")
  val getHistoryByIdQuery = SQL("SELECT * from ticket_history th WHERE id > {id} AND ticket_id={ticket_id} ORDER BY th.date_occurred ASC LIMIT 1") // XXX id is only safe here if greater ids are always greater in date
  val getFollowingHistoryQuery = SQL("SELECT * from ticket_history th WHERE id={id}")
  val getHistoryQuery = SQL("SELECT * from ticket_history th WHERE ticket_id={ticket_id} ORDER BY th.date_occurred ASC LIMIT {offset},{count}")
  val getHistoryCountQuery = SQL("SELECT count(*) FROM ticket_history WHERE ticket_id={ticket_id}")
  val getMostRecentHistoryIdQuery = SQL("SELECT th.id FROM ticket_history th WHERE ticket_id={ticket_id} ORDER BY th.date_occurred DESC LIMIT 1")

  val getAllCommentsQuery = SQL("SELECT * FROM ticket_comments tc JOIN users u ON u.id = tc.user_id WHERE ticket_id={ticket_id} ORDER by tc.date_created ASC")
  val getCommentsQuery = SQL("SELECT * FROM ticket_comments tc JOIN users u ON u.id = tc.user_id WHERE ticket_id={ticket_id} ORDER BY tc.date_created ASC LIMIT {offset},{count}")
  val getCommentsCountQuery = SQL("SELECT count(*) FROM ticket_comments WHERE ticket_id={ticket_id}")
  val insertHistoryQuery = SQL("INSERT INTO ticket_history (user_id, ticket_id, project_id, priority_id, resolution_id, proposed_resolution_id, reporter_id, severity_id, status_id, type_id, position, summary, description, date_occurred) SELECT {user_id}, t.id, t.project_id, t.priority_id, t.resolution_id, t.proposed_resolution_id, t.reporter_id, t.severity_id, t.status_id, t.type_id, t.position, t.summary, t.description, UTC_TIMESTAMP() FROM tickets t WHERE t.id={ticket_id}")
  val getCommentFacetUser = SQL("SELECT count(*) as occurrences, u.username, u.id FROM ticket_comments tc JOIN users u ON u.id = tc.user_id WHERE ticket_id={ticket_id} GROUP BY user_id")

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
    get[Option[String]]("description") ~
    get[Date]("date_created") map {
      case id~reporterId~projectId~priorityId~resolutionId~proposedResolutionId~severityId~statusId~typeId~position~summary~description~dateCreated => Ticket(
        id, reporterId, projectId, priorityId, resolutionId, proposedResolutionId, severityId, statusId, typeId, position, summary, description, dateCreated
      )
    }
  }

  val editTicket = {
    get[Pk[Long]]("id") ~
    get[Long]("reporter_id") ~
    get[Long]("project_id") ~
    get[Long]("priority_id") ~
    get[Option[Long]]("resolution_id") ~
    get[Option[Long]]("proposed_resolution_id") ~
    get[Long]("severity_id") ~
    get[Long]("type_id") ~
    get[Option[Long]]("position") ~
    get[String]("summary") ~
    get[Option[String]]("description") map {
      case id~reporterId~projectId~priorityId~resolutionId~proposedResolutionId~severityId~typeId~position~summary~description => EditTicket(
        id, reporterId, projectId, priorityId, resolutionId, proposedResolutionId, severityId, typeId, position, summary, description
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
    get[Option[String]]("tickets.description") ~
    get[Date]("tickets.date_created") map {
      case id~reporterId~projectId~projectName~priorityId~priorityName~resolutionId~proposedResolutionId~severityId~severityName~workflowStatusId~statusId~statusName~typeId~typeName~position~summary~description~dateCreated => FullTicket(
        id, reporterId, projectId, projectName, priorityId, priorityName, resolutionId, proposedResolutionId, severityId, severityName, workflowStatusId, statusId, statusName, typeId, typeName, position, summary, description, dateCreated
      )
    }
  }

  val history = {
    get[Pk[Long]]("ticket_history.id") ~
    get[Long]("ticket_history.ticket_id") ~
    get[Long]("ticket_history.user_id") ~
    get[Long]("ticket_history.reporter_id") ~
    get[Long]("ticket_history.project_id") ~
    get[Long]("ticket_history.priority_id") ~
    get[Option[Long]]("ticket_history.resolution_id") ~
    get[Option[Long]]("ticket_history.proposed_resolution_id") ~
    get[Long]("ticket_history.severity_id") ~
    get[Long]("ticket_history.status_id") ~
    get[Long]("ticket_history.type_id") ~
    get[Option[Long]]("ticket_history.position") ~
    get[String]("ticket_history.summary") ~
    get[Option[String]]("ticket_history.description") ~
    get[Date]("ticket_history.date_occurred") map {
      case id~ticketId~userId~reporterId~projectId~priorityId~resolutionId~proposedResolutionId~severityId~statusId~typeId~position~summary~description~dateCreated => TicketHistory(
        id, ticketId, userId, reporterId, projectId, priorityId, resolutionId, proposedResolutionId, severityId, statusId, typeId, position, summary, description, dateCreated
      )
    }
  }

  val comment = {
    get[Pk[Long]]("id") ~
    get[Long]("user_id") ~
    get[String]("username") ~
    get[String]("realname") ~
    get[Long]("ticket_id") ~
    get[String]("content") ~
    get[Date]("date_created") map {
      case id~userId~username~realName~ticketId~content~dateCreated => Comment(id, userId, username, realName, ticketId, content, dateCreated)
    }
  }

  val authorFacet = {
    get[String]("users.username") ~
    get[Long]("users.id") ~
    get[Long]("occurrences") map {
      case username~authorId~occurrences => Facet(username, authorId.toString, occurrences)
    }
  }

  def addComment(ticketId: Long, userId: Long, content: String) : Boolean = {
    
    val ticket = this.getById(ticketId)
    
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
          this.getById(id)
        }
      }
      case None => None
    }

    result
  }
  
  def delete(id: Long) {
      
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

  def getHistoryById(id: Long) : Option[TicketHistory] = {
    DB.withConnection { implicit conn =>
      getHistoryByIdQuery.on('id -> id).as(history.singleOpt)
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
      val comments = getCommentsQuery.on(
        'ticket_id-> ticketId,
        'offset   -> offset,
        'count    -> count
      ).as(comment *)
      
      val totalRows = getCommentsCountQuery.on('ticket_id -> ticketId).as(scalar[Long].single)
      
      Page(comments, page, count, totalRows)
    }
  }
  
  def getCommentsAsSearchResult(ticketId: Long, page: Int = 0, count: Int = 10) : SearchResult[Comment] = {
    
    DB.withConnection { implicit conn =>
      val comments = getAllCommentsQuery.on('ticket_id -> ticketId).as(comment *)

      val afacets = getCommentFacetUser.on('ticket_id -> ticketId).as(authorFacet *)

      SearchResult(
        pager = this.getComments(ticketId, page, count),
        facets = List(
          Facets(name = "Author", "author", afacets)
        )
      )
    }
  }

  def getHistory(ticketId: Long, page: Int = 0, count: Int = 10) : Page[TicketHistory] = {
    
    val offset = count * page
    
    DB.withConnection { implicit conn =>
      val histories = getHistoryQuery.on(
        'ticket_id-> ticketId,
        'offset   -> offset,
        'count    -> count
      ).as(history *)
      
      val totalRows = getHistoryCountQuery.on('ticket_id -> ticketId).as(scalar[Long].single)
      
      Page(histories, page, count, totalRows)
    }
  }

  def getChanges(ticketId: Long, histories: Seq[TicketHistory]) : Seq[Seq[TicketChange]] = {
    
    val firstChanges = DB.withConnection { implicit conn =>
      val highest = histories.last
      val newestHistoryId = highest.id.get // XXX last or first?
      val mostRecent = getMostRecentHistoryIdQuery.on('ticket_id -> ticketId).as(scalar[Long].single)
      (newestHistoryId == mostRecent) match {
        case true => {
          // This is the most recent history entry, so we need to fetch the
          // ticket and make our first change via that comparison
          val ticket = this.getById(ticketId).get
          // Now we've got to return None so that the map that follows will
          // compare this history entry to it's predecessor
          val changes = new ListBuffer[TicketChange]
          if(highest.severityId != ticket.severityId) {
            changes.append(TicketChange("ticket.severity", highest.severityId.toString, ticket.severityId.toString, highest.dateOccurred))
          }
          changes
        }
        case false => 
          // This isn't the most recent history entry, so fetch the one after
          // this one and use it for our comparison
          val old  = getHistoryByIdQuery.on('id -> newestHistoryId, 'ticket_id -> ticketId).as(history.singleOpt).get
          this.computeChange(old, highest)
      }
    }
    var lastHistory : Option[TicketHistory] = None
    val changes = histories.reverseMap { history => {
      val hcs = lastHistory match {
        case Some(old) => this.computeChange(old, history)
        case None => firstChanges
      }
      lastHistory = Some(history)
      hcs
    } }
    
    println(changes)
    changes
  }
  
  def computeChange(older: TicketHistory, newer: TicketHistory) : Seq[TicketChange] = {

    val changes = new ListBuffer[TicketChange]
    if(older.severityId != newer.severityId) {
      changes.append(
        TicketChange("ticket.severity", older.severityId.toString, newer.severityId.toString, older.dateOccurred)
      )
    }
    changes
  }

  // def getChangesAsSearchResult(ticketId: Long, page: Int = 0, count: Int = 10) : SearchResult[Change] = {
  //   
  //   DB.withConnection { implicit conn =>
  //     val comments = getAllCommentsQuery.on('ticket_id -> ticketId).as(comment *)
  // 
  //     val afacets = getCommentFacetUser.on('ticket_id -> ticketId).as(authorFacet *)
  // 
  //     SearchResult(
  //       pager = this.getHistory(ticketId, page, count),
  //       facets = List(
  //         Facets(name = "Author", "author", afacets)
  //       )
  //     )
  //   }
  // }
  // 
  // def getChanges(ticketId: Long, page: Int = 0, count: Int = 10) : Page[Change] = {
  //   
  //   DB.withConnection { implicit conn =>
  //     val changes = getChangesQuery.on(
  //       'ticket_id-> ticketId,
  //       'offset   -> offset,
  //       'count    -> count
  //     ).as(change *)
  //   }
  //   
  //   val totalRows = getChangesCountQuery.on('ticket_id -> ticketId).as(scalar[Long].single)
  //   
  //   Page(changes, page, count, totalRows)
  // }

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
  
  def update(userId: Long, id: Long, ticket: EditTicket) = {

    DB.withTransaction { implicit conn =>

      insertHistoryQuery.on(
        'user_id -> userId,
        'ticket_id -> id
      ).executeUpdate

      updateQuery.on(
        'id                     -> id,
        'reporter_id            -> ticket.reporterId,
        'priority_id            -> ticket.priorityId,
        'resolution_id          -> ticket.resolutionId,
        'proposed_resolution_id -> ticket.proposedResolutionId,
        'severity_id            -> ticket.severityId,
        'type_id                -> ticket.typeId,
        'description            -> ticket.description,
        'position               -> ticket.position,
        'summary                -> ticket.summary
      ).executeUpdate
    }
  }
}