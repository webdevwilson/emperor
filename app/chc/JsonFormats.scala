package chc

import anorm.Id
import java.text.SimpleDateFormat
import java.util.Date
import models._
import org.clapper.markwrap._
import play.api.i18n.Messages
import play.api.libs.json.Json._
import play.api.libs.json._

object JsonFormats {

  val dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")
  val markdown = MarkWrap.parserFor(MarkupType.Markdown)

  // XXX UNIT TESTS FOR THE LOVE OF GOD

  /**
   * JSON conversion for Comment
   */
  implicit object CommentFormat extends Format[Comment] {
    def reads(json: JsValue): Comment = Comment(
      id = Id((json \ "id").as[Long]),
      userId = (json \ "user_id").as[Long],
      username = (json \ "user_name").as[String],
      realName = (json \ "user_realname").as[String],
      ticketId = (json \ "ticket_id").as[String],
      content = (json \ "content").as[String],
      dateCreated = new Date() // XXX
    )

    def writes(comment: Comment): JsValue = {
      val cdoc: Map[String,JsValue] = Map(
        "ticket_id"     -> JsString(comment.ticketId),
        "user_id"       -> JsNumber(comment.userId),
        "user_realname" -> JsString(comment.realName),
        "content"       -> JsString(comment.content),
        "date_created"  -> JsString(dateFormatter.format(comment.dateCreated))
      )
      toJson(cdoc)
    }
  }

  implicit object EditTicketFormat extends Format[EditTicket] {
    def reads(json: JsValue): EditTicket = EditTicket(
      ticketId      = Id((json \ "ticket_id").as[String]),
      reporterId    = (json \ "reporter_id").as[Long],
      assigneeId    = (json \ "assignee_id").as[Option[Long]],
      priorityId    = (json \ "priority_id").as[Long],
      projectId     = (json \ "project_id").as[Long],
      resolutionId  = (json \ "resolution_id").as[Option[Long]],
      severityId    = (json \ "severity_id").as[Long],
      typeId        = (json \ "type_id").as[Long],
      summary       = (json \ "summary").as[String],
      description   = (json \ "description").as[Option[String]],
      attentionId    = (json \ "attention_id").as[Option[Long]],
      position      = (json \ "position").as[Option[Long]],
      proposedResolutionId = (json \ "proposed_resolution_id").as[Option[Long]]
    )

    def writes(ticket: EditTicket): JsValue = {

      val assId  = ticket.assigneeId match {
        case Some(assId)=> JsNumber(assId)
        case None       => JsNull
      }
      val resId = ticket.resolutionId match {
        case Some(id)   => JsNumber(id)
        case None       => JsNull
      }
      val propResId = ticket.proposedResolutionId match {
        case Some(id)   => JsNumber(id)
        case None       => JsNull
      }
      val pos = ticket.position match {
        case Some(id)   => JsNumber(id)
        case None       => JsNull
      }
      val desc = ticket.description match {
        case Some(d)    => JsString(d)
        case None       => JsNull
      }

      val tdoc: Map[String,JsValue] = Map(
        "ticket_id"   -> JsString(ticket.ticketId.get),
        "reporter_id" -> JsNumber(ticket.reporterId),
        "assignee_id" -> assId,
        "priority_id" -> JsNumber(ticket.priorityId),
        "resolution_id" -> resId,
        "proposed_resolution_id" -> propResId,
        "severity_id" -> JsNumber(ticket.severityId),
        "type_id"     -> JsNumber(ticket.typeId),
        "position"    -> pos,
        "summary"     -> JsString(ticket.summary),
        "description" -> desc
      )
      toJson(tdoc)
    }
  }

  /**
   * JSON conversion for Event
   */
  implicit object EventFormat extends Format[Event] {
    def reads(json: JsValue): Event = Event(
      projectId = (json \ "project_id").as[Long],
      projectName = (json \ "project_name").as[String],
      userId = (json \ "user_id").as[Long],
      userRealName = (json \ "user_realname").as[String],
      eKey = (json \ "ekey").as[String],
      eType = (json \ "etype").as[String],
      content = (json \ "content").as[String],
      url = (json \ "url").as[String],
      dateCreated = new Date() // XXX
    )

    def writes(event: Event): JsValue = {
      val edoc: Map[String,JsValue] = Map(
        "project_id"    -> JsNumber(event.projectId),
        "project_name"  -> JsString(event.projectName),
        "user_id"       -> JsNumber(event.userId),
        "user_realname" -> JsString(event.userRealName),
        "ekey"          -> JsString(event.eKey),
        "etype"         -> JsString(event.eType),
        "content"       -> JsString(event.content),
        "url"           -> JsString(event.url),
        "date_created"  -> JsString(dateFormatter.format(event.dateCreated))
      )
      toJson(edoc)
    }
  }

  /**
   * JSON conversion for FullTicket
   */
  implicit object FullTicketFormat extends Format[FullTicket] {

    def reads(json: JsValue): FullTicket = FullTicket(
      id        = Id((json \ "id").as[Long]),
      ticketId  = (json \ "ticket_id").as[String],
      user      = NamedThing(
        id    = (json \ "user_id").as[Long],
        name  = (json \ "user_name").as[String]
      ),
      reporter  = NamedThing(
        id    = (json \ "reporter_id").as[Long],
        name  = (json \ "reporter_name").as[String]
      ),
      assignee = OptionalNamedThing(
        id    = Some((json \ "assignee_id").as[Long]),
        name  = Some((json \ "assignee_name").as[String])
      ),
      attention = OptionalNamedThing(
        id    = Some((json \ "attention_id").as[Long]),
        name  = Some((json \ "attention_name").as[String])
      ),
      project  = NamedThing(
        id    = (json \ "project_id").as[Long],
        name  = (json \ "project_id").as[String]
      ),
      priority  = ColoredThing(
        id    = (json \ "priority_id").as[Long],
        name  = (json \ "priority_name").as[String],
        color = (json \ "priority_color").as[String]
      ),
      resolution = OptionalNamedThing(
        id    = Some((json \ "resolution_id").as[Long]),
        name  = Some((json \ "resolution_name").as[String])
      ),
      proposedResolution = OptionalNamedThing(
        id    = Some((json \ "proposed_resolution_id").as[Long]),
        name  = Some((json \ "proposed_resolution_name").as[String])
      ),
      severity  = ColoredThing(
        id    = (json \ "severity_id").as[Long],
        name  = (json \ "severity_name").as[String],
        color = (json \ "severity_color").as[String]
      ),
      workflowStatusId = (json \ "workflow_status_id").as[Long],
      status  = NamedThing(
        id    = (json \ "status_id").as[Long],
        name  = (json \ "status_name").as[String]
      ),
      ttype  = ColoredThing(
        id    = (json \ "type_id").as[Long],
        name  = (json \ "type_name").as[String],
        color = (json \ "type_color").as[String]
      ),
      position = Some((json \ "position").as[Long]),
      summary = (json \ "summary").as[String],
      description = Some((json \ "description").as[String]),
      dateCreated = new Date() // XXX
    )

    def writes(ticket: FullTicket): JsValue = {

      val resId = ticket.resolution.id match {
        case Some(id)   => JsNumber(id)
        case None       => JsNull
      }
      val resName = ticket.resolution.name match {
        case Some(name) => name
        case None       => "TICK_RESO_UNRESOLVED"
      }
      val propResId = ticket.proposedResolution.id match {
        case Some(id)   => JsNumber(id)
        case None       => JsNull
      }
      val propResName = ticket.proposedResolution.name match {
        case Some(name) => name
        case None       => "TICK_RESO_UNRESOLVED"
      }
      val assId = ticket.assignee.id match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      }
      val assName = ticket.assignee.name match {
        case Some(name) => JsString(name)
        case None       => JsNull
      }
      val attId = ticket.attention.id match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      }
      val attName = ticket.attention.name match {
        case Some(name) => JsString(name)
        case None       => JsNull
      }
      val tdoc: Map[String,JsValue] = Map(
        "id"              -> JsNumber(ticket.id.get),
        "ticket_id"       -> JsString(ticket.ticketId),
        "project_id"      -> JsNumber(ticket.project.id),
        "project_name"    -> JsString(ticket.project.name),
        "priority_id"     -> JsNumber(ticket.priority.id),
        "priority_name"   -> JsString(ticket.priority.name),
        "priority_name_i18n" -> JsString(Messages(ticket.priority.name)),
        "priority_color"  -> JsString(ticket.priority.color),
        "resolution_id"   -> resId,
        "resolution_name" -> JsString(Messages(resName)),
        "proposed_resolution_id" -> propResId,
        "proposed_resolution_name" -> JsString(propResName),
        "proposed_resolution_name_i18n" -> JsString(Messages(propResName)),
        "reporter_id"     -> JsNumber(ticket.reporter.id),
        "reporter_name"   -> JsString(ticket.reporter.name),
        "assignee_id"     -> assId,
        "assignee_name"   -> assName,
        "attention_id"    -> attId,
        "attention_name"  -> attName,
        "severity_id"     -> JsNumber(ticket.severity.id),
        "severity_color"  -> JsString(ticket.severity.color),
        "severity_name"   -> JsString(ticket.severity.name),
        "severity_name_i18n" -> JsString(Messages(ticket.severity.name)),
        "status_id"       -> JsNumber(ticket.status.id),
        "status_name"     -> JsString(ticket.status.name),
        "status_name_i18n"-> JsString(Messages(ticket.status.name)),
        "type_id"         -> JsNumber(ticket.ttype.id),
        "type_color"      -> JsString(ticket.ttype.color),
        "type_name"       -> JsString(ticket.ttype.name),
        "type_name_i18n"  -> JsString(Messages(ticket.ttype.name)),
        "summary"         -> JsString(ticket.summary),
        "description"     -> JsString(markdown.parseToHTML(ticket.description.getOrElse(""))),
        "date_created"    -> JsString(dateFormatter.format(ticket.dateCreated))
      )
      toJson(tdoc)
    }
  }

  /**
   * JSON conversion for WorkflowStatus
   */
  implicit object WorkflowStatusFormat extends Format[WorkflowStatus] {

    def reads(json: JsValue): WorkflowStatus = WorkflowStatus(
      id          = Id((json \ "id").as[Long]),
      workflowId  = (json \ "workflow_id").as[Long],
      statusId    = (json \ "status_id").as[Long],
      name        = (json \ "name").as[String],
      position    = (json \ "position").as[Int]
    )

    def writes(ws: WorkflowStatus): JsValue = {

      val wsdoc: Map[String,JsValue] = Map(
        "id"              -> JsNumber(ws.id.get),
        "workflow_id"     -> JsNumber(ws.workflowId),
        "status_id"       -> JsNumber(ws.statusId),
        "name"            -> JsString(ws.name),
        "name_i18n"       -> JsString(Messages(ws.name)),
        "position"        -> JsNumber(ws.position)
      )
      toJson(wsdoc)
    }
  }

  /**
   * JSON conversion for Link
   */
  implicit object LinkFormat extends Format[Link] {

    def reads(json: JsValue): Link = Link(
      id          = Id((json \ "id").as[Long]),
      typeId      = (json \ "type_id").as[Long],
      typeName    = (json \ "name").as[String],
      parentId    = (json \ "parent_id").as[String],
      parentSummary = (json \ "parent_summary").as[String],
      childId     = (json \ "parent_id").as[String],
      childSummary = (json \ "parent_summary").as[String],
      dateCreated = new Date() // XXX
    )

    def writes(l: Link): JsValue = {

      val ldoc: Map[String,JsValue] = Map(
        "id"              -> JsNumber(l.id.get),
        "type_id"         -> JsNumber(l.typeId),
        "name"            -> JsString(l.typeName),
        "name_i18n"       -> JsString(Messages(l.typeName)),
        "parent_id"       -> JsString(l.parentId),
        "child_id"        -> JsString(l.childId),
        "child_summary"   -> JsString(l.childSummary),
        "parent_summary"  -> JsString(l.parentSummary),
        "date_created"    -> JsString(dateFormatter.format(l.dateCreated))
      )
      toJson(ldoc)
    }
  }

  /**
   * JSON conversion for Project
   */
  implicit object ProjectFormat extends Format[Project] {

    def reads(json: JsValue): Project = Project(
      id          = Id((json \ "id").as[Long]),
      workflowId  = (json \ "workflow_id").as[Long],
      name        = (json \ "name").as[String],
      key         = (json \ "key").as[String],
      dateCreated = new Date() // XXX
    )

    def writes(obj: Project): JsValue = {

      val doc: Map[String,JsValue] = Map(
        "id"              -> JsNumber(obj.id.get),
        "workflow_id"     -> JsNumber(obj.workflowId),
        "name"            -> JsString(obj.name),
        "key"             -> JsString(obj.key),
        "sequence_current"-> JsNumber(obj.sequenceCurrent),
        "date_created"    -> JsString(dateFormatter.format(obj.dateCreated))
      )
      toJson(doc)
    }
  }
}