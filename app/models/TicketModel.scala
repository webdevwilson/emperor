package models

import anorm._
import anorm.SqlParser._
import emp.util.Pagination.Page
import emp.event._
import java.util.Date
import java.util.regex.Pattern
import play.api.db.DB
import play.api.Play.current
import scala.collection.mutable.ListBuffer

/**
 * Class for a comment.
 */
case class Comment(
  id: Pk[Long] = NotAssigned, userId: Long, username: String,
  realName: String, ticketId: String, content: String, dateCreated: Date
)

/**
 * Class for a status change.  Used with status change form.
 */
case class StatusChange(
  statusId: Long, comment: Option[String]
)

/**
 * Class for assignment.  Used with assignment form.
 */
case class Assignment(
  userId: Option[Long], comment: Option[String]
)

/**
 * Class for resolution.  Used with resolution form.
 */
case class Resolution(
  resolutionId: Long, comment: Option[String]
)

/**
 * Case class for a link between tickets.
 */
case class Link(
  id: Pk[Long] = NotAssigned, typeId: Long, typeName: String,
  parentId: String, childId: String,
  dateCreated: Date
)

/**
 * Case class for a link between tickets with more information culled from
 * the tickets themselves.
 */
case class FullLink(
  id: Pk[Long] = NotAssigned, typeId: Long, typeName: String,
  parentId: String, parentSummary: String, parentResolutionId: Option[Long],
  childId: String, childSummary: String, childResolutionId: Option[Long],
  dateCreated: Date
)

/**
 * Class for creating a ticket.  Eliminates fields that aren't useful
 * at creation time.
 */
case class InitialTicket(
  reporterId: Long, assigneeId: Option[Long] = None, projectId: Long,
  priorityId: Long, severityId: Long, typeId: Long, position: Option[Long] = None,
  summary: String, description: Option[String] = None
)

/**
 * Class for editing a ticket.  Eliminates fields that aren't useful when editing.
 */
case class EditTicket(
  ticketId: Pk[String] = NotAssigned, reporterId: Long, assigneeId: Option[Long],
  attentionId: Option[Long], projectId: Long,
  priorityId: Long, resolutionId: Option[Long],
  proposedResolutionId: Option[Long], severityId: Long,
  typeId: Long, position: Option[Long], summary: String,
  description: Option[String]
)

/**
 * Class for getting all possible information out of a ticket. Uses other case classes
 * to avoid the 22 limit.
 */
case class FullTicket(
  id: Pk[Long] = NotAssigned, ticketId: String, user: NamedThing, reporter: NamedThing,
  assignee: OptionalNamedThing, attention: OptionalNamedThing,
  project: NamedThing,  priority: ColoredPositionedThing,
  resolution: OptionalNamedThing,
  proposedResolution: OptionalNamedThing,
  severity: ColoredPositionedThing, workflowStatusId: Long, status: NamedThing,
  ttype: ColoredThing, position: Option[Long],
  summary: String, description: Option[String], dateCreated: Date
) {
  def abbreviatedSummary(length: Int = 15) = summary match {
    case x if x.length > length => x.take(length) + "&hellip;"
    case x => x
  }
}

/**
 * Class for getting a ticket.
 */
case class Ticket(
  id: Pk[Long] = NotAssigned, ticketId: String, reporterId: Long, assigneeId: Long,
  attentionId: Long, projectId: Long, priorityId: Long,
  resolutionId: Option[Long], proposedResolutionId: Option[Long],
  severityId: Long, statusId: Long, typeId: Long, position: Option[Long],
  summary: String, description: Option[String], dateCreated: Date
)

/**
 * A thing with a name and id.
 */
case class NamedThing(
  id: Long, name: String
)

/**
 * A thing with an id, name and a color.
 */
case class ColoredThing(
  id: Long, name: String, color: String
)

/**
 * A thing with an id, name, priority and a color.
 */
case class ColoredPositionedThing(
  id: Long, name: String, color: String, position: Int
)

/**
 * An optional thing with an id and name.
 */
case class OptionalNamedThing(
  id: Option[Long], name: Option[String]
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
  val deleteQuery = SQL("DELETE FROM tickets WHERE ticket_id={ticket_id}")

  val insertLinkQuery = SQL("INSERT IGNORE INTO ticket_links (link_type_id, parent_ticket_id, child_ticket_id, date_created) VALUES ({link_type_id}, {parent_ticket_id}, {child_ticket_id}, UTC_TIMESTAMP())")
  val getLinksQuery = SQL("SELECT * FROM ticket_links JOIN ticket_link_types ON ticket_link_types.id = ticket_links.link_type_id WHERE parent_ticket_id={ticket_id} OR child_ticket_id={ticket_id} GROUP BY ticket_links.id ORDER BY ticket_links.date_created")
  val getLinkByIdQuery = SQL("SELECT * FROM ticket_links JOIN ticket_link_types ON ticket_link_types.id = ticket_links.link_type_id WHERE ticket_links.id={id}")
  val deleteLinkQuery = SQL("DELETE FROM ticket_links WHERE id={id}")

  val getByProjectQuery = SQL("SELECT * FROM tickets WHERE project_id={project_id}")
  val getCountByProjectQuery = SQL("SELECT COUNT(*) FROM tickets WHERE project_id={project_id}")

  val getOpenByProjectQuery = SQL("SELECT * FROM tickets WHERE project_id={project_id} AND resolution_id IS NULL")
  val getCountOpenByProjectQuery = SQL("SELECT COUNT(*) FROM tickets WHERE project_id={project_id} AND resolution_id IS NULL")

  val getByProjectAndStatusQuery = SQL("SELECT * FROM tickets WHERE project_id={project_id} AND status_id={status_id}")
  val getCountByProjectAndStatusQuery = SQL("SELECT COUNT(*) FROM tickets WHERE project_id={project_id} AND status_id={status_id}")

  val getCountTodayByProjectQuery = SQL("SELECT COUNT(*) FROM tickets WHERE project_id={project_id} AND DATE(date_created) = DATE(NOW())")
  val getCountThisWeekByProjectQuery = SQL("SELECT COUNT(*) FROM tickets WHERE project_id={project_id} AND DATE(date_created) + 7 > DATE(NOW()) ")

  // parser for retrieving a ticket
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

  // parser for retrieving an EditTicket
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

  // Parser for retrieving a FullTicket
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
    get[Int]("priority_position") ~
    get[Option[Long]]("resolution_id") ~
    get[Option[String]]("resolution_name") ~
    get[Option[Long]]("proposed_resolution_id") ~
    get[Option[String]]("proposed_resolution_name") ~
    get[Long]("severity_id") ~
    get[String]("severity_name") ~
    get[String]("severity_color") ~
    get[Int]("severity_position") ~
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
      case id~tickId~userId~userName~repId~repName~assId~assName~attId~attName~projId~projName~priId~priName~priColor~priPos~resId~resName~propResId~propResName~sevId~sevName~sevColor~sevPos~statusId~workflowStatusId~statusName~typeId~typeName~typeColor~position~summary~description~dateCreated =>
        FullTicket(
          id = id,
          ticketId = tickId,
          user = NamedThing(userId, userName),
          reporter = NamedThing(repId, repName),
          assignee = OptionalNamedThing(assId, assName),
          attention = OptionalNamedThing(attId, attName),
          project = NamedThing(projId, projName),
          priority = ColoredPositionedThing(priId, priName, priColor, priPos),
          resolution = OptionalNamedThing(resId, resName),
          proposedResolution = OptionalNamedThing(propResId, propResName),
          severity = ColoredPositionedThing(sevId, sevName, sevColor, sevPos),
          workflowStatusId = workflowStatusId,
          status = NamedThing(statusId, statusName),
          ttype = ColoredThing(typeId, typeName, typeColor),
          position = position,
          summary = summary,
          description = description,
          dateCreated = dateCreated
        )
    }
  }

  // Parser for retrieving a comment
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

  // Parser for retrieving a link
  val link = {
    get[Pk[Long]]("ticket_links.id") ~
    get[Long]("ticket_links.link_type_id") ~
    get[String]("ticket_link_types.name") ~
    get[String]("ticket_links.parent_ticket_id") ~
    get[String]("ticket_links.child_ticket_id") ~
    get[Date]("ticket_links.date_created") map {
      case id~linkId~linkName~parentId~childId~dateCreated => Link(
        id = id, typeId = linkId, typeName = linkName,
        parentId = parentId, childId = childId,
        dateCreated = dateCreated
      )
    }
  }

  val idPattern = Pattern.compile("^\\p{L}{1}[\\p{Nd}|\\p{L}]*-\\d+")

  /**
   * Verifies that a string is a valid ticket id via regex.
   */
  def isValidTicketId(id: String): Boolean = idPattern.matcher(id).matches

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

          EmperorEventBus.publish(
            CommentTicketEvent(
              ticketId = ticketId
            )
          )

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

  /**
   * Assign a ticket with an optional comment.
   */
  def assign(ticketId: String, userId: Long, assigneeId: Option[Long], comment: Option[String] = None): FullTicket = {
    val tick = this.getById(ticketId).get

    val assigned = tick.copy(assigneeId = assigneeId)
    val ft = this.update(userId = userId, id = ticketId, ticket = assigned, comment = comment)
    SearchModel.indexTicket(ticket = ft)
    ft
  }

  /**
   * Mark a ticket as resolved with an optional comment.
   */
  def resolve(ticketId: String, userId: Long, resolutionId: Long, comment: Option[String] = None): FullTicket = {
    val tick = this.getById(ticketId).get

    val ft = this.update(userId = userId, id = ticketId, ticket = tick, resolutionId = Some(resolutionId), comment = comment)
    SearchModel.indexTicket(ticket = ft)
    ft
  }

  /**
   * Remove the resolution of a ticket with an optional comment.
   */
  def unresolve(ticketId: String, userId: Long, comment: Option[String] = None): FullTicket = {
      val tick = this.getById(ticketId).get

      val ft = this.update(userId = userId, id = ticketId, ticket = tick, resolutionId = None, clearResolution = true, comment = comment)
      SearchModel.indexTicket(ticket = ft)
      ft
  }

  /**
   * Change the status of a ticket.  Is really a wrapper around `update`.
   */
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
          val nt = this.getFullById(ticketId)

          nt.map { t =>
            SearchModel.indexTicket(ticket = t)

            SearchModel.indexEvent(Event(
              projectId     = t.project.id,
              projectName   = t.project.name,
              userId        = t.user.id,
              userRealName  = t.user.name,
              eKey          = t.ticketId,
              eType         = "ticket_create",
              content       = t.summary,
              url           = "",
              dateCreated   = t.dateCreated
            ))
            // Get on the bus!
            EmperorEventBus.publish(
              NewTicketEvent(
                ticketId = t.ticketId
              )
            )
          }
          nt
        }
      }
      case None => None
    }

    result
  }

  /**
   * Delete a ticket.
   */
  def delete(ticketId: String) {
    DB.withConnection { implicit conn =>
      deleteQuery.on('ticket_id -> ticketId).execute
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

  /**
   * Get links for a ticket.
   */
  def getLinks(id: String): List[FullLink] = {

    DB.withConnection { implicit conn =>
      val links = getLinksQuery.on('ticket_id -> id).as(link *)

      links.map { link =>

        // XXX This sucks.  I would love to fix this, but I can't turn
        // the link query into a JOIN to the full_tickets view because it
        // hangs MySQL. Ugh. - gphat
        val parent = getFullById(link.parentId).get
        val child = getFullById(link.childId).get

        FullLink(
          id            = link.id,
          typeId        = link.typeId,
          typeName      = link.typeName,
          parentId      = link.parentId,
          parentSummary = parent.summary,
          parentResolutionId = parent.resolution.id,
          childId       = link.childId,
          childSummary  = child.summary,
          childResolutionId = child.resolution.id,
          dateCreated   = link.dateCreated
        )
      }
    }
  }

  /**
   * Get a FullLink by id.
   */
  def getFullLinkById(id: Long): Option[FullLink] = {

    DB.withConnection { implicit conn =>
      val maybeL = getLinkByIdQuery.on('id -> id).as(link.singleOpt)
      maybeL match {
        case Some(l) => {
          val parent = getFullById(l.parentId).get
          val child = getFullById(l.childId).get

          Some(FullLink(
            id            = l.id,
            typeId        = l.typeId,
            typeName      = l.typeName,
            parentId      = l.parentId,
            parentSummary = parent.summary,
            parentResolutionId = parent.resolution.id,
            childId       = l.childId,
            childSummary  = child.summary,
            childResolutionId = child.resolution.id,
            dateCreated   = l.dateCreated
          ))
        }
        case None => None
      }
    }
  }

  /**
   * Get a Link by id.
   */
  def getLinkById(id: Long): Option[Link] = {

    DB.withConnection { implicit conn =>
      getLinkByIdQuery.on('id -> id).as(link.singleOpt)
    }
  }

  /**
   * Link a child ticket to a parent with a type.
   */
  def link(linkTypeId: Long, parentId: String, childId: String): Option[FullLink] = {

    DB.withConnection { implicit conn =>
      val li = insertLinkQuery.on(
        'link_type_id     -> linkTypeId,
        'parent_ticket_id -> parentId,
        'child_ticket_id  -> childId
      ).executeInsert()
      li.map({ lid =>
        EmperorEventBus.publish(
          LinkTicketEvent(
            parentId = parentId,
            childId = childId
          )
        )
        getFullLinkById(lid)
      }).getOrElse(None)
    }
  }

  /**
   * Remove a link between tickets.
   */
  def removeLink(id: Long) {
    DB.withConnection { implicit conn =>
      val link = getFullLinkById(id)
      link.map({ l =>
        deleteLinkQuery.on('id -> id).execute()
        EmperorEventBus.publish(
          UnlinkTicketEvent(
            parentId = l.parentId,
            childId = l.childId
          )
        )
      })
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

  /**
   * Change the contents of a ticket. The resolution, status and clear resolution
   * provide the caller with the ability to manipulate these fields directly, as they
   * are special fields that do not normally get modified.  The `clearResolution` field
   * allows the clearing of a resolution.  The optional comment will add a comment in
   * addition to other changes.
   * Note that if there is no change, nothing will happen here.
   */
  def update(
    userId: Long, id: String, ticket: EditTicket,
    resolutionId: Option[Long] = None, statusId: Option[Long] = None,
    clearResolution: Boolean = false,
    comment: Option[String] = None
  ): FullTicket = {

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

    val changed = if(oldTicket.project.id != ticket.projectId) {
      true
    } else if(oldTicket.priority.id != ticket.priorityId) {
      true
    } else if(oldTicket.resolution.id != newResId) {
      true
    } else if(oldTicket.proposedResolution.id != ticket.proposedResolutionId) {
      true
    } else if(oldTicket.reporter.id != ticket.reporterId) {
      true
    } else if(oldTicket.assignee.id != ticket.assigneeId) {
      true
    } else if(oldTicket.attention.id != ticket.attentionId) {
      true
    } else if(oldTicket.severity.id != ticket.severityId) {
      true
    } else if(oldTicket.status.id != statusId.getOrElse(oldTicket.status.id)) {
      true
    } else if(oldTicket.ttype.id != ticket.typeId) {
      true
    } else if(oldTicket.description != ticket.description) {
      true
    } else if(oldTicket.summary != ticket.summary) {
      true
    } else {
      false
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
        comment.map { content =>
          val comm = addComment(ticketId = id, userId = userId, content = content)
          SearchModel.indexComment(comm.get)
        }

        // Get on the bus!
        EmperorEventBus.publish(
          ChangeTicketEvent(
            ticketId = id,
            // This logic should probably be tested… XXX
            resolved = if(changed && (oldTicket.resolution.id != newResId)) true else false,
            unresolved = if(clearResolution && !oldTicket.resolution.id.isEmpty) true else false
          )
        )
      }

      val newTicket = DB.withConnection { implicit conn =>
        getFullById(id).get
      }

      if(changed) {
        SearchModel.indexHistory(newTick = newTicket, oldTick = oldTicket)
      }
      newTicket
    } else {
      oldTicket
    }
  }
}
