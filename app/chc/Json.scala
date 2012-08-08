package chc

import anorm.Id
import java.text.SimpleDateFormat
import java.util.Date
import models._
import play.api.libs.json.Json._
import play.api.libs.json._

object Json {

  val dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")

  // XXX UNIT TESTS FOR THE LOVE OF GOD

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
        id    = (json \ "ttype_id").as[Long],
        name  = (json \ "ttype_name").as[String],
        color = (json \ "ttype_color").as[String]
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
        case Some(name) => JsString(name)
        case None       => JsString("TICK_RESO_UNRESOLVED")
      }
      val propResId = ticket.proposedResolution.id match {
        case Some(id)   => JsNumber(id)
        case None       => JsNull
      }
      val propResName = ticket.proposedResolution.name match {
        case Some(name) => JsString(name)
        case None       => JsString("TICK_RESO_UNRESOLVED")
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
        "project_id"      -> JsNumber(ticket.project.id),
        "project_name"    -> JsString(ticket.project.name),
        "priority_id"     -> JsNumber(ticket.priority.id),
        "priority_name"   -> JsString(ticket.priority.name),
        "priority_color"  -> JsString(ticket.priority.color),
        "resolution_id"   -> resId,
        "resolution_name" -> resName,
        "proposed_resolution_id" -> propResId,
        "proposed_resolution_name" -> propResName,
        "reporter_id"     -> JsNumber(ticket.reporter.id),
        "reporter_name"   -> JsString(ticket.reporter.name),
        "assignee_id"     -> assId,
        "assignee_name"   -> assName,
        "attention_id"    -> attId,
        "attention_name"  -> attName,
        "severity_id"     -> JsNumber(ticket.severity.id),
        "severity_color"  -> JsString(ticket.severity.name),
        "severity_name"   -> JsString(ticket.severity.name),
        "status_id"       -> JsNumber(ticket.status.id),
        "status_name"     -> JsString(ticket.status.name),
        "type_id"         -> JsNumber(ticket.ttype.id),
        "type_color"      -> JsString(ticket.ttype.color),
        "type_name"       -> JsString(ticket.ttype.name),
        "summary"         -> JsString(ticket.summary),
        "description"     -> JsString(ticket.description.getOrElse("")),
        "date_created"    -> JsString(dateFormatter.format(ticket.dateCreated))
      )
      toJson(tdoc)
    }
  }
}