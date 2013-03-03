package models

import anorm._
import anorm.SqlParser._
import emp.util.AnormExtension._
import emp.util.Pagination.Page
import emp.event._
import org.joda.time.{DateTime,DateTimeZone,Days}
import java.util.regex.Pattern
import org.joda.time.DateTime
import play.api.db.DB
import play.api.Play.current
import scala.collection.mutable.ListBuffer

/**
 * Class for a comment.
 */
case class Comment(
  id: Pk[Long] = NotAssigned, ctype: String, userId: Long, username: String,
  realName: String, ticketId: Long, content: String, dateCreated: DateTime
)

/**
 * Class for a making a link. Used with link form.
 */
case class MakeLink(
  linkTypeId: Long,
  tickets: List[String],
  comment: Option[String]
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
  parentId: Long, childId: Long,
  dateCreated: DateTime
)

/**
 * Case class for a link between tickets with more information culled from
 * the tickets themselves.
 */
case class FullLink(
  id: Pk[Long] = NotAssigned, typeId: Long, typeName: String,
  parentId: Long, parentSummary: String, parentResolutionId: Option[Long],
  childId: Long, childSummary: String, childResolutionId: Option[Long],
  dateCreated: DateTime
)

/**
 * Convenience class for creating new tickets.
 */
case class NewTicket(
  projectId: Long,
  typeId: Long,
  priorityId: Long,
  severityId: Long,
  summary: String,
  description: Option[String],
  assigneeId: Option[Long] = None,
  position: Option[Long] = None
)

/**
 * Class for getting all possible information out of a ticket. Uses other case classes
 * to avoid the 22 limit.
 */
case class FullTicket(
  id: Pk[Long] = NotAssigned, projectTicketId: Long, user: NamedThing,
  assignee: OptionalNamedThing, attention: OptionalNamedThing,
  project: NamedKeyedThing,  priority: ColoredPositionedThing,
  resolution: OptionalNamedThing,
  severity: ColoredPositionedThing, workflowStatusId: Long, status: NamedThing,
  ttype: ColoredThing, position: Option[Long],
  summary: String, description: Option[String], dateCreated: DateTime
) {
  def abbreviatedSummary(length: Int = 15) = summary match {
    case x if x.length > length => x.take(length) + "&hellip;"
    case x => x
  }

  /**
   * Returns true if this ticket has a resolution.
   */
  def isResolved = this.resolution.id.isDefined

  /**
   * Returns the number of days since this ticket was created.  If the ticket
   * is resolved then returns the number of days since the last change.
   */
  def daysOpen = Days.daysBetween(
    dateCreated,
    // If resolved, use last change date, else use now.
    if(isResolved) {
      dateCreated
    } else {
      new DateTime(DateTimeZone.UTC)
    }
  ).getDays

  /**
   *
   */
  def daysSinceLastChange = Days.daysBetween(dateCreated, new DateTime(DateTimeZone.UTC)).getDays

  def ticketId = project.key + "-" + this.projectTicketId
}

/**
 * Class for getting a ticket.
 */
case class Ticket(
  id: Pk[Long] = NotAssigned, userId: Long,
  projectId: Long, projectTicketId: Long,
  dateCreated: DateTime
)

case class TicketData(
  id: Pk[Long] = NotAssigned,
  ticketId: Long,
  userId: Long,
  priorityId: Long,
  resolutionId: Option[Long] = None,
  assigneeId: Option[Long],
  attentionId: Option[Long] = None,
  severityId: Long,
  statusId: Long,
  typeId: Long,
  position: Option[Long],
  summary: String,
  description: Option[String],
  dateCreated: DateTime
)

/**
 * A thing with a name and id.
 */
case class NamedThing(
  id: Long, name: String
)

/**
 * A thing with a name and id.
 */
case class NamedKeyedThing(
  id: Long, name: String, key: String
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
  val getIdQuery = SQL("SELECT id FROM tickets WHERE project_id={project_id} AND project_ticket_id={project_ticket_id}")
  val getByIdQuery = SQL("SELECT * FROM tickets WHERE id={id}")
  val getDataByIdQuery = SQL("SELECT * FROM ticket_data WHERE id={id}")
  val getAllCurrentQuery = SQL("SELECT * FROM full_tickets ORDER BY date_created DESC")
  val getFullByIdQuery = SQL("SELECT * FROM full_tickets WHERE id={id}")
  val getAllFullByIdQuery = SQL("SELECT * FROM full_all_tickets t  WHERE t.ticket_id={ticket_id} ORDER BY date_created ASC")
  val getAllFullByIdCountQuery = SQL("SELECT COUNT(*) FROM full_all_tickets  WHERE ticket_id={ticket_id} ORDER BY date_created ASC")
  val listQuery = SQL("SELECT * FROM tickets LIMIT {count} OFFSET {offset}")
  val listCountQuery = SQL("SELECT count(*) FROM tickets")
  val insertQuery = SQL("INSERT INTO tickets (user_id, project_id, project_ticket_id) VALUES ({user_id}, {project_id}, {project_ticket_id})")
  val insertDataQuery = SQL("INSERT INTO ticket_data (ticket_id, user_id, priority_id, resolution_id, assignee_id, attention_id, severity_id, status_id, type_id, position, summary, description) VALUES ({ticket_id}, {user_id}, {priority_id}, {resolution_id}, {assignee_id}, {attention_id}, {severity_id}, {status_id}, {type_id}, {position}, {summary}, {description})")
  val getCommentByIdQuery = SQL("SELECT * FROM ticket_comments JOIN users ON users.id = ticket_comments.user_id WHERE ticket_comments.id={id} ORDER BY ticket_comments.date_created")
  val insertCommentQuery = SQL("INSERT INTO ticket_comments (type, user_id, ticket_id, content) VALUES ({type}, {user_id}, {ticket_id}, {content})")
  val deleteCommentQuery = SQL("DELETE FROM ticket_comments WHERE id={id}")
  val deleteQuery = SQL("DELETE FROM tickets WHERE id={id}")
  val deleteDataQuery = SQL("DELETE FROM ticket_data WHERE ticket_id={ticket_id}")

  val insertLinkQuery = SQL("INSERT INTO ticket_links (link_type_id, parent_ticket_id, child_ticket_id) VALUES ({link_type_id}, {parent_ticket_id}, {child_ticket_id})")
  val getLinksQuery = SQL("SELECT * FROM ticket_links JOIN ticket_link_types ON ticket_link_types.id = ticket_links.link_type_id WHERE parent_ticket_id={ticket_id} OR child_ticket_id={ticket_id} GROUP BY ticket_links.id ORDER BY link_type_id, ticket_links.date_created ASC")
  val getLinkByIdQuery = SQL("SELECT * FROM ticket_links JOIN ticket_link_types ON ticket_link_types.id = ticket_links.link_type_id WHERE ticket_links.id={id}")
  val deleteLinkQuery = SQL("DELETE FROM ticket_links WHERE id={id}")

  // parser for retrieving a ticket
  val ticket = {
    get[Pk[Long]]("id") ~
    get[Long]("user_id") ~
    get[Long]("project_id") ~
    get[Long]("project_ticket_id") ~
    get[DateTime]("date_created") map {
      case id~userId~projId~projTickId~dateCreated => Ticket(
        id = id,
        userId = userId,
        projectId = projId,
        projectTicketId = projTickId,
        dateCreated = dateCreated
      )
    }
  }

  // parser for retrieving a ticket
  val ticketData = {
    get[Pk[Long]]("id") ~
    get[Long]("ticket_id") ~
    get[Long]("user_id") ~
    get[Long]("priority_id") ~
    get[Option[Long]]("resolution_id") ~
    get[Option[Long]]("assignee_id") ~
    get[Option[Long]]("attention_id") ~
    get[Long]("severity_id") ~
    get[Long]("status_id") ~
    get[Long]("type_id") ~
    get[Option[Long]]("position") ~
    get[String]("summary") ~
    get[Option[String]]("description") ~
    get[DateTime]("date_created") map {
      case id~tickId~userId~priId~resId~assId~attId~sevId~statId~typeId~position~summary~description~dateCreated => TicketData(
        id = id,
        ticketId = tickId,
        userId = userId,
        priorityId = priId,
        resolutionId = resId,
        assigneeId = assId,
        attentionId = attId,
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


  // Parser for retrieving a FullTicket
  val fullTicket = {
    get[Pk[Long]]("id") ~
    get[Long]("project_ticket_id") ~
    get[Long]("user_id") ~
    get[String]("user_realname") ~
    get[Option[Long]]("assignee_id") ~
    get[Option[String]]("assignee_realname") ~
    get[Option[Long]]("attention_id") ~
    get[Option[String]]("attention_realname") ~
    get[Long]("project_id") ~
    get[String]("project_name") ~
    get[String]("project_key") ~
    get[Long]("priority_id") ~
    get[String]("priority_name") ~
    get[String]("priority_color") ~
    get[Int]("priority_position") ~
    get[Option[Long]]("resolution_id") ~
    get[Option[String]]("resolution_name") ~
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
    get[DateTime]("date_created") map {
      case id~projTickId~userId~userName~assId~assName~attId~attName~projId~projName~projKey~priId~priName~priColor~priPos~resId~resName~sevId~sevName~sevColor~sevPos~statusId~workflowStatusId~statusName~typeId~typeName~typeColor~position~summary~description~dateCreated =>
        FullTicket(
          id = id,
          projectTicketId = projTickId,
          user = NamedThing(userId, userName),
          assignee = OptionalNamedThing(assId, assName),
          attention = OptionalNamedThing(attId, attName),
          project = NamedKeyedThing(projId, projName, projKey),
          priority = ColoredPositionedThing(priId, priName, priColor, priPos),
          resolution = OptionalNamedThing(resId, resName),
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
    get[String]("type") ~
    get[Long]("user_id") ~
    get[String]("username") ~
    get[String]("realname") ~
    get[Long]("ticket_id") ~
    get[String]("content") ~
    get[DateTime]("ticket_comments.date_created") map {
      case id~ctype~userId~username~realName~ticketId~content~dateCreated => Comment(id, ctype, userId, username, realName, ticketId, content, dateCreated)
    }
  }

  // Parser for retrieving a link
  val link = {
    get[Pk[Long]]("ticket_links.id") ~
    get[Long]("ticket_links.link_type_id") ~
    get[String]("ticket_link_types.name") ~
    get[Long]("ticket_links.parent_ticket_id") ~
    get[Long]("ticket_links.child_ticket_id") ~
    get[DateTime]("ticket_links.date_created") map {
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
   * Turn a ticket id string into it's constituent parts.
   */
  def parseTicketId(id: String): Option[Tuple2[String, Long]] = {
    isValidTicketId(id) match {
      case false => None
      case true  => {
        val pos = id.indexOf("-")
        Some((id.substring(0, pos), id.substring(pos + 1).toLong))
      }
    }
  }

  /**
   * Get the ID of a ticket via it's string-id.
   */
  def getActualId(id: String): Option[Long] = {
    parseTicketId(id).flatMap({ parts =>
      ProjectModel.getByKey(parts._1).map({ proj =>
        DB.withConnection { implicit conn =>
          getIdQuery.on(
            'project_id -> proj.id.get,
            'project_ticket_id -> parts._2.toLong
          ).as(scalar[Long].single)
        }
      })
    })
  }

  /**
   * Add a comment.
   */
  def addComment(ticketId: Long, ctype: String, userId: Long, content: String) : Option[Comment] = {

    val ticket = this.getById(ticketId)

    ticket match {
      case Some(ticket) => {
        DB.withConnection { implicit conn =>
          val id = insertCommentQuery.on(
            'type       -> ctype,
            'user_id    -> userId,
            'ticket_id  -> ticketId,
            'content    -> content
          ).executeInsert()

          val comm = getCommentById(id.get)
          comm.map { c =>
            EmperorEventBus.publish(
              CommentTicketEvent(
                ticketId = ticketId,
                commentId = c.id.get
              )
            )
          }
          comm
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
  def assign(ticketId: Long, userId: Long, assigneeId: Option[Long], comment: Option[String] = None): FullTicket = {
    val tick = this.getDataById(ticketId).get

    val assigned = tick.copy(assigneeId = assigneeId)
    this.update(userId = userId, id = ticketId, ticket = assigned, comment = comment)
  }

  /**
   * Mark a ticket as resolved with an optional comment.
   */
  def resolve(ticketId: Long, userId: Long, resolutionId: Long, comment: Option[String] = None): FullTicket = {

    val tick = this.getDataById(ticketId).get

    val ft = this.update(userId = userId, id = ticketId, ticket = tick, resolutionId = Some(resolutionId), comment = comment)
    ft
  }

  /**
   * Remove the resolution of a ticket with an optional comment.
   */
  def unresolve(ticketId: Long, userId: Long, comment: Option[String] = None): FullTicket = {

    val tick = this.getDataById(ticketId).get

    val ft = this.update(userId = userId, id = ticketId, ticket = tick, resolutionId = None, clearResolution = true, comment = comment)
    ft
  }

  /**
   * Change the status of a ticket.  Is really a wrapper around `update`.
   */
  def changeStatus(ticketId: Long, newStatusId: Long, userId: Long, comment: Option[String] = None) = {

    DB.withConnection { implicit conn =>

      val tick = this.getDataById(ticketId).get

      this.update(userId = userId, id = ticketId, ticket = tick, statusId = Some(newStatusId), comment = comment)
    }
  }

  /**
   * Create a ticket.
   */
  def create(userId: Long, projectId: Long, typeId: Long, priorityId: Long, severityId: Long, summary: String, description: Option[String] = None,
    assigneeId: Option[Long] = None, attentionId: Option[Long] = None, position: Option[Long] = None
  ): Option[FullTicket] = {

    // If this is missing? XXX
    val project = ProjectModel.getById(projectId)

    // Fetch the starting status we should use for the project's workflow.
    val startingStatus = project match {
      case Some(x) => WorkflowModel.getStartingStatus(x.workflowId)
      case None => None
    }
    // XXX Should log something up there, really

    val result = startingStatus match {
      case Some(status) => {
        DB.withConnection { implicit conn =>

          val tid = ProjectModel.getNextSequence(projectId)

          val id = insertQuery.on(
            'user_id      -> userId,
            'project_id   -> projectId,
            'project_ticket_id -> tid
          ).executeInsert()

          val tdid = insertDataQuery.on(
            'ticket_id    -> id,
            'user_id      -> userId,
            'priority_id  -> priorityId,
            'resolution_id-> None,
            'assignee_id  -> assigneeId,
            'attention_id -> attentionId,
            'severity_id  -> severityId,
            'status_id    -> status.id,
            'type_id      -> typeId,
            'position     -> position,
            'description  -> description,
            'summary      -> summary
          ).executeInsert()

          // XXX Might not be here?
          val nt = this.getFullById(id.get)

          nt.map { t =>
            // Get on the bus!
            EmperorEventBus.publish(
              NewTicketEvent(
                ticketId = t.id.get
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
  def delete(id: Long) {

    DB.withTransaction { implicit conn =>
      deleteDataQuery.on('ticket_id -> id).execute
      deleteQuery.on('id -> id).execute
    }
  }

  /**
   * Get a comment by id.
   */
  def getCommentById(id: Long): Option[Comment] = {

    DB.withConnection { implicit conn =>
      getCommentByIdQuery.on('id -> id).as(comment.singleOpt)
    }
  }

  /**
   * Get ticket by ticketId.
   */
  def getById(id: Long): Option[Ticket] = {

    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(ticket.singleOpt)
    }
  }

  /**
   * Convenience method for getting a ticket by it's string id.
   */
  def getByStringId(id: String): Option[Ticket] = {
    getActualId(id).flatMap({ tid =>
      getById(tid)
    })
  }

  /**
   * Convenience method for getting a full ticket by it's string id.
   */
  def getFullByStringId(id: String): Option[FullTicket] = {
    getActualId(id).flatMap({ tid =>
      getFullById(tid)
    })
  }

  /**
   * Get ticket data by ticketId.
   */
  def getDataById(id: Long): Option[TicketData] = {

    DB.withConnection { implicit conn =>
      getByIdQuery.on('ticket_id -> id).as(ticketData.singleOpt)
    }
  }

  /**
   * Get ticket by ticketId.  This version returns the `FullTicket`.
   */
  def getFullById(id: Long): Option[FullTicket] = {

    DB.withConnection { implicit conn =>
      getFullByIdQuery.on('id -> id).as(fullTicket.singleOpt)
    }
  }

  def getAllCurrent: List[Ticket] = {

    DB.withConnection { implicit conn =>
      allQuery.as(ticket.*)
    }
  }

  def getAllCurrentFull: List[FullTicket] = {
    DB.withConnection { implicit conn =>
      getAllCurrentQuery.as(fullTicket.*)
    }
  }

  def getAllComments: List[Comment] = {

    DB.withConnection { implicit conn =>
      allCommentsQuery.as(comment.*)
    }
  }

  def getAllFullById(id: String): List[FullTicket] = {

    DB.withConnection { implicit conn =>
      getAllFullByIdQuery.on('ticket_id -> id).as(fullTicket.*)
    }
  }

  def getAllFullCountById(id: String): Long = {

    DB.withConnection { implicit conn =>
      getAllFullByIdCountQuery.on('ticket_id -> id).as(scalar[Long].single)
    }
  }

  /**
   * Get links for a ticket.
   */
  def getLinks(id: String): List[FullLink] = {

    DB.withConnection { implicit conn =>
      val links = getLinksQuery.on('ticket_id -> id).as(link.*)

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

          println(l.parentId)

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
  def link(linkTypeId: Long, parentId: Long, childId: Long): Option[FullLink] = {

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
      ).as(ticket.*)

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
    userId: Long, id: Long, ticket: TicketData,
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

    val changed = if(oldTicket.priority.id != ticket.priorityId) {
      true
    } else if(oldTicket.resolution.id != newResId) {
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

        insertDataQuery.on(
          'ticket_id              -> id,
          'user_id                -> userId,
          'priority_id            -> ticket.priorityId,
          'resolution_id          -> newResId,
          'assignee_id            -> ticket.assigneeId,
          'attention_id           -> ticket.attentionId,
          'severity_id            -> ticket.severityId,
          'status_id              -> statusId.getOrElse(oldTicket.status.id),
          'type_id                -> ticket.typeId,
          'position               -> ticket.position,
          'description            -> ticket.description,
          'summary                -> ticket.summary
        ).executeInsert()

        // Add a comment, if we had one.
        comment.map { content =>
          val comm = addComment(ticketId = id, ctype = "comment", userId = userId, content = content)
        }
      }

      val newTicket = DB.withConnection { implicit conn =>
        getFullById(id).get
      }

      if(changed) {
        // Get on the bus!
        EmperorEventBus.publish(
          ChangeTicketEvent(
            ticketId = id,
            oldTicketId = oldTicket.id.get,
            newTicketId = newTicket.id.get,
            // This logic should probably be tested… XXX
            resolved = if(changed && (oldTicket.resolution.id != newResId)) true else false,
            unresolved = if(clearResolution && !oldTicket.resolution.id.isEmpty) true else false
          )
        )
      }
      newTicket
    } else {
      oldTicket
    }
  }
}
