package emp

import anorm.{Id,NotAssigned}
import emp.text.Renderer
import models._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import play.api.i18n.Messages
import play.api.libs.json.Json._
import play.api.libs.json._

/**
 * Code for converting Emperor entities into JSON.
 */
object JsonFormats {

  val dateFormatter = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss'Z'")
  val dateFormatterUTC = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss'Z'").withZoneUTC()

  private def optionLongtoJsValue(maybeId: Option[Long]) = maybeId.map({ l => JsNumber(l) }).getOrElse(JsNull)

  private def optionI18nStringtoJsValue(maybeId: Option[String]) = maybeId.map({ s => JsString(Messages(s)) }).getOrElse(JsNull)
  private def optionStringtoJsValue(maybeId: Option[String]) = maybeId.map({ s => JsString(s) }).getOrElse(JsNull)

  /**
   * JSON conversion for Comment
   */
  implicit object CommentFormat extends Format[Comment] {
    def reads(json: JsValue): JsResult[Comment] = JsSuccess(Comment(
      id = Id((json \ "id").as[Long]),
      ctype = (json \ "type").as[String],
      userId = (json \ "user_id").as[Long],
      username = (json \ "user_name").as[String],
      realName = (json \ "user_realname").as[String],
      ticketId = (json \ "ticket_id").as[String],
      content = (json \ "content").as[String],
      dateCreated = (json \ "date_created").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

    def writes(comment: Comment): JsValue = {
      val cdoc: Map[String,JsValue] = Map(
        "id"            -> JsNumber(comment.id.get),
        "type"          -> JsString(comment.ctype),
        "ticket_id"     -> JsString(comment.ticketId),
        "user_id"       -> JsNumber(comment.userId),
        "user_name"     -> JsString(comment.username),
        "user_realname" -> JsString(comment.realName),
        "content"       -> JsString(comment.content),
        "date_created"  -> JsString(dateFormatter.print(comment.dateCreated))
      )
      toJson(cdoc)
    }
  }

  implicit object EditTicketFormat extends Format[EditTicket] {
    def reads(json: JsValue): JsResult[EditTicket] = JsSuccess(EditTicket(
      ticketId      = (json \ "ticketId").as[Option[String]].map({ id => Id(id) }).getOrElse(NotAssigned),
      reporterId    = (json \ "reporterId").as[Long],
      assigneeId    = (json \ "assigneeId").as[Option[Long]],
      priorityId    = (json \ "priorityId").as[Long],
      projectId     = (json \ "projectId").as[Long],
      resolutionId  = (json \ "resolutionId").as[Option[Long]],
      severityId    = (json \ "severityId").as[Long],
      typeId        = (json \ "typeId").as[Long],
      summary       = (json \ "summary").as[String],
      description   = (json \ "description").as[Option[String]],
      attentionId    = (json \ "attentionId").as[Option[Long]],
      position      = (json \ "position").as[Option[Long]],
      proposedResolutionId = (json \ "proposedResolutionId").as[Option[Long]]
    ))

    def writes(ticket: EditTicket): JsValue = {

      val tdoc: Map[String,JsValue] = Map(
        "ticketId"   -> JsString(ticket.ticketId.get),
        "reporterId" -> JsNumber(ticket.reporterId),
        "assigneeId" -> optionLongtoJsValue(ticket.assigneeId),
        "priorityId" -> JsNumber(ticket.priorityId),
        "resolutionId" -> optionLongtoJsValue(ticket.resolutionId),
        "proposedResolutionId" -> optionLongtoJsValue(ticket.proposedResolutionId),
        "severityId" -> JsNumber(ticket.severityId),
        "typeId"     -> JsNumber(ticket.typeId),
        "position"    -> optionLongtoJsValue(ticket.position),
        "summary"     -> JsString(ticket.summary),
        "description" -> optionStringtoJsValue(ticket.description)
      )
      toJson(tdoc)
    }
  }

  /**
   * JSON conversion for Event
   */
  implicit object EventFormat extends Format[Event] {
    def reads(json: JsValue): JsResult[Event] = JsSuccess(Event(
      projectId = (json \ "project_id").as[Long],
      projectName = (json \ "project_name").as[String],
      userId = (json \ "user_id").as[Long],
      userRealName = (json \ "user_realname").as[String],
      eKey = (json \ "ekey").as[String],
      eType = (json \ "etype").as[String],
      content = (json \ "content").as[String],
      url = (json \ "url").as[String],
      dateCreated = (json \ "date_created").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

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
        "date_created"  -> JsString(dateFormatter.print(event.dateCreated))
      )
      toJson(edoc)
    }
  }

  /**
   * JSON conversion for FullLink
   */
  implicit object FullLinkFormat extends Format[FullLink] {

    def reads(json: JsValue): JsResult[FullLink] = JsSuccess(FullLink(
      id          = Id((json \ "id").as[Long]),
      typeId      = (json \ "type_id").as[Long],
      typeName    = (json \ "type_name").as[String],
      parentId    = (json \ "parent_id").as[String],
      parentResolutionId = (json \ "parent_resolution_id").as[Option[Long]],
      parentSummary = (json \ "parent_summary").as[String],
      childId     = (json \ "parent_id").as[String],
      childResolutionId = (json \ "child_resolution_id").as[Option[Long]],
      childSummary = (json \ "child_summary").as[String],
      dateCreated = (json \ "date_created").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

    def writes(l: FullLink): JsValue = {

      val childRes = l.childResolutionId match {
        case Some(reso) => JsNumber(reso)
        case None => JsNull
      }

      val parentRes = l.parentResolutionId match {
        case Some(reso) => JsNumber(reso)
        case None => JsNull
      }

      val ldoc: Map[String,JsValue] = Map(
        "id"              -> JsNumber(l.id.get),
        "type_id"         -> JsNumber(l.typeId),
        "type_name"       -> JsString(l.typeName),
        "type_name_i18n"  -> JsString(Messages(l.typeName)),
        "type_name_i18n_inverted" -> JsString(Messages(l.typeName + "_INVERT")),
        "parent_id"       -> JsString(l.parentId),
        "parent_resolution_id" -> parentRes,
        "parent_summary"  -> JsString(l.parentSummary),
        "child_id"        -> JsString(l.childId),
        "child_resolution_id" -> childRes,
        "child_summary"   -> JsString(l.childSummary),
        "date_created"    -> JsString(dateFormatter.print(l.dateCreated))
      )
      toJson(ldoc)
    }
  }

  /**
   * JSON conversion for FullTicket
   */
  implicit object FullTicketFormat extends Format[FullTicket] {

    // XX This is allllll wrong.  To be able to inflate it should verify
    // all these IDs then poll the database for the values.
    def reads(json: JsValue): JsResult[FullTicket] = JsSuccess(FullTicket(
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
        id    = (json \ "assignee_id").as[Option[Long]],
        name  = (json \ "assignee_name").as[Option[String]]
      ),
      attention = OptionalNamedThing(
        id    = (json \ "attention_id").as[Option[Long]],
        name  = (json \ "attention_name").as[Option[String]]
      ),
      project  = NamedThing(
        id    = (json \ "project_id").as[Long],
        name  = (json \ "project_name").as[String]
      ),
      priority  = ColoredPositionedThing(
        id    = (json \ "priority_id").as[Long],
        name  = (json \ "priority_name").as[String],
        color = (json \ "priority_color").as[String],
        position = (json \ "priority_position").as[Int]
      ),
      resolution = OptionalNamedThing(
        id    = (json \ "resolution_id").as[Option[Long]],
        name  = (json \ "resolution_name").as[Option[String]]
      ),
      proposedResolution = OptionalNamedThing(
        id    = (json \ "proposed_resolution_id").as[Option[Long]],
        name  = (json \ "proposed_resolution_name").as[Option[String]]
      ),
      severity  = ColoredPositionedThing(
        id    = (json \ "severity_id").as[Long],
        name  = (json \ "severity_name").as[String],
        color = (json \ "severity_color").as[String],
        position = (json \ "severity_position").as[Int]
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
      position = (json \ "position").as[Option[Long]],
      summary = (json \ "summary").as[String],
      description = (json \ "description").as[Option[String]],
      dateCreated = (json \ "date_created").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime()),
      originalDateCreated = (json \ "date_created").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

    def writes(ticket: FullTicket): JsValue = {

      val tdoc: Map[String,JsValue] = Map(
        "id"              -> JsNumber(ticket.id.get),
        "ticket_id"       -> JsString(ticket.ticketId),
        "project_id"      -> JsNumber(ticket.project.id),
        "project_name"    -> JsString(ticket.project.name),
        "priority_id"     -> JsNumber(ticket.priority.id),
        "priority_name"   -> JsString(ticket.priority.name),
        "priority_name_i18n" -> JsString(Messages(ticket.priority.name)),
        "priority_color"  -> JsString(ticket.priority.color),
        "priority_position" -> JsNumber(ticket.priority.position),
        "resolution_id"   -> optionLongtoJsValue(ticket.resolution.id),
        // A ticket with no resolution gets a default name, hence the differing logic here
        "resolution_name" -> JsString(ticket.resolution.name.getOrElse("TICK_RESO_UNRESOLVED")),
        "resolution_name_i18n" -> JsString(Messages(ticket.resolution.name.getOrElse("TICK_RESO_UNRESOLVED"))),
        "proposed_resolution_id" -> optionLongtoJsValue(ticket.proposedResolution.id),
        "proposed_resolution_name" -> optionStringtoJsValue(ticket.proposedResolution.name),
        "proposed_resolution_name_i18n" -> optionI18nStringtoJsValue(ticket.proposedResolution.name),
        "reporter_id"     -> JsNumber(ticket.reporter.id),
        "reporter_name"   -> JsString(ticket.reporter.name),
        "assignee_id"     -> optionLongtoJsValue(ticket.assignee.id),
        "assignee_name"   -> optionStringtoJsValue(ticket.assignee.name),
        "attention_id"    -> optionLongtoJsValue(ticket.attention.id),
        "attention_name"  -> optionStringtoJsValue(ticket.attention.name),
        "severity_id"     -> JsNumber(ticket.severity.id),
        "severity_color"  -> JsString(ticket.severity.color),
        "severity_name"   -> JsString(ticket.severity.name),
        "severity_name_i18n" -> JsString(Messages(ticket.severity.name)),
        "severity_position" -> JsNumber(ticket.severity.position),
        "status_id"       -> JsNumber(ticket.status.id),
        "status_name"     -> JsString(ticket.status.name),
        "status_name_i18n"-> JsString(Messages(ticket.status.name)),
        "type_id"         -> JsNumber(ticket.ttype.id),
        "type_color"      -> JsString(ticket.ttype.color),
        "type_name"       -> JsString(ticket.ttype.name),
        "type_name_i18n"  -> JsString(Messages(ticket.ttype.name)),
        "user_id"         -> JsNumber(ticket.user.id),
        "user_name"       -> JsString(ticket.user.name),
        "summary"         -> JsString(ticket.summary),
        "short_summary"   -> JsString(ticket.abbreviatedSummary()),
        "workflow_status_id" -> JsNumber(ticket.workflowStatusId),
        "description"     -> JsString(Renderer.render(ticket.description)),
        "date_created"    -> JsString(dateFormatter.print(ticket.dateCreated)),
        "original_date_created" -> JsString(dateFormatter.print(ticket.originalDateCreated))
      )
      toJson(tdoc)
    }
  }

  /**
   * JSON conversion for Group
   */
  implicit object GroupFormat extends Format[Group] {

    def reads(json: JsValue): JsResult[Group] = JsSuccess(Group(
      id          = Id((json \ "id").as[Long]),
      name        = (json \ "name").as[String],
      dateCreated = (json \ "date_created").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

    def writes(obj: Group): JsValue = {

      val doc: Map[String,JsValue] = Map(
        "id"          -> JsNumber(obj.id.get),
        "name"        -> JsString(obj.name),
        "nameI18N"    -> JsString(Messages(obj.name)),
        "dateCreated" -> JsString(dateFormatter.print(obj.dateCreated))
      )
      toJson(doc)
    }
  }

  /**
   * JSON conversion for GroupUser
   */
  implicit object GroupUserFormat extends Format[GroupUser] {

    def reads(json: JsValue): JsResult[GroupUser] = JsSuccess(GroupUser(
      id          = Id((json \ "id").as[Long]),
      groupId     = (json \ "groupId").as[Long],
      userId      = (json \ "userId").as[Long],
      username    = (json \ "username").as[String],
      realName    = (json \ "realName").as[String],
      dateCreated = (json \ "date_created").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

    def writes(obj: GroupUser): JsValue = {

      val doc: Map[String,JsValue] = Map(
        "id"          -> JsNumber(obj.id.get),
        "groupId"     -> JsNumber(obj.groupId),
        "userId"      -> JsNumber(obj.userId),
        "username"    -> JsString(obj.username),
        "realName"    -> JsString(obj.realName),
        "realNameI18N"-> JsString(Messages(obj.realName)),
        "dateCreated" -> JsString(dateFormatter.print(obj.dateCreated))
      )
      toJson(doc)
    }
  }


  /**
   * JSON conversion for Link
   */
  implicit object LinkFormat extends Format[Link] {

    def reads(json: JsValue): JsResult[Link] = JsSuccess(Link(
      id          = Id((json \ "id").as[Long]),
      typeId      = (json \ "type_id").as[Long],
      typeName    = (json \ "name").as[String],
      parentId    = (json \ "parent_id").as[String],
      childId     = (json \ "child_id").as[String],
      dateCreated = (json \ "date_created").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

    def writes(l: Link): JsValue = {

      val ldoc: Map[String,JsValue] = Map(
        "id"              -> JsNumber(l.id.get),
        "type_id"         -> JsNumber(l.typeId),
        "name"            -> JsString(l.typeName),
        "name_i18n"       -> JsString(Messages(l.typeName)),
        "parent_id"       -> JsString(l.parentId),
        "child_id"        -> JsString(l.childId),
        "date_created"    -> JsString(dateFormatter.print(l.dateCreated))
      )
      toJson(ldoc)
    }
  }

  /**
   * JSON conversion for Permission
   */
  implicit object PermissionFormat extends Format[Permission] {

    // This should be a boolean (global)
    def reads(json: JsValue): JsResult[Permission] = JsSuccess(Permission(
      name  = (json \ "name").as[String],
      global = (json \ "global").as[Boolean]
    ))

    def writes(obj: Permission): JsValue = {
      val doc: Map[String,JsValue] = Map(
        "id"          -> JsString(obj.name),
        "name"        -> JsString(obj.name),
        "nameI18N"    -> JsString(Messages(obj.name)),
        "description" -> JsString(obj.name + "_DESC"),
        "descriptionI18N" -> JsString(Messages(obj.name + "_DESC")),
        "global"      -> JsBoolean(obj.global)
      )
      toJson(doc)
    }
  }

  /**
   * JSON conversion for PermissionSchemeGroup
   */
  implicit object PermissionSchemeGroupFormat extends Format[PermissionSchemeGroup] {

    // This should be a boolean (global)
    def reads(json: JsValue): JsResult[PermissionSchemeGroup] = JsSuccess(PermissionSchemeGroup(
      id                = Id((json \ "id").as[Long]),
      permissionSchemeId= (json \ "permissionSchemeId").as[Long],
      permissionId      = (json \ "permissionId").as[String],
      groupId           = (json \ "groupId").as[Long],
      groupName         = (json \ "groupName").as[String],
      dateCreated       = (json \ "dateCreated").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

    def writes(obj: PermissionSchemeGroup): JsValue = {
      val doc: Map[String,JsValue] = Map(
        "id"                -> JsNumber(obj.id.get),
        "permissionSchemeId"-> JsNumber(obj.permissionSchemeId),
        "permissionId"      -> JsString(obj.permissionId),
        "groupId"           -> JsNumber(obj.groupId),
        "groupName"         -> JsString(obj.groupName),
        "dateCreated"       -> JsString(dateFormatter.print(obj.dateCreated))
      )
      toJson(doc)
    }
  }

  /**
   * JSON conversion for PermissionSchemeUser
   */
  implicit object PermissionSchemeUserFormat extends Format[PermissionSchemeUser] {

    // This should be a boolean (global)
    def reads(json: JsValue): JsResult[PermissionSchemeUser] = JsSuccess(PermissionSchemeUser(
      id                = Id((json \ "id").as[Long]),
      permissionSchemeId= (json \ "permissionSchemeId").as[Long],
      permissionId      = (json \ "permissionId").as[String],
      userId            = (json \ "userId").as[Long],
      username          = (json \ "userName").as[String],
      realName          = (json \ "realName").as[String],
      dateCreated       = (json \ "dateCreated").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

    def writes(obj: PermissionSchemeUser): JsValue = {
      val doc: Map[String,JsValue] = Map(
        "id"                -> JsNumber(obj.id.get),
        "permissionSchemeId"-> JsNumber(obj.permissionSchemeId),
        "permissionId"      -> JsString(obj.permissionId),
        "userId"            -> JsNumber(obj.userId),
        "username"          -> JsString(obj.username),
        "realName"          -> JsString(obj.realName),
        "dateCreated"       -> JsString(dateFormatter.print(obj.dateCreated))
      )
      toJson(doc)
    }
  }

  implicit object PermissionSchemeFormat extends Format[PermissionScheme] {

    def reads(json: JsValue): JsResult[PermissionScheme] = JsSuccess(PermissionScheme(
      id          = Id((json \ "id").as[Long]),
      name        = (json \ "name").as[String],
      description = (json \ "description").as[Option[String]],
      dateCreated = (json \ "dateCreated").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

    def writes(obj: PermissionScheme): JsValue = {
      val doc: Map[String,JsValue] = Map(
        "id"          -> JsNumber(obj.id.get),
        "name"        -> JsString(obj.name),
        "nameI18N"    -> JsString(Messages(obj.name + "_DESC")),
        "description" -> optionStringtoJsValue(obj.description),
        "dateCreated" -> JsString(dateFormatter.print(obj.dateCreated))
      )
      toJson(doc)
    }
  }

  /**
   * JSON conversion for Project
   */
  implicit object ProjectFormat extends Format[Project] {

    def reads(json: JsValue): JsResult[Project] = JsSuccess(Project(
      id          = Id((json \ "id").as[Long]),
      workflowId  = (json \ "workflowId").as[Long],
      name        = (json \ "name").as[String],
      key         = (json \ "key").as[String],
      ownerId     = (json \ "owner_id").as[Option[Long]],
      permissionSchemeId = (json \ "permissionSchemeId").as[Long],
      defaultPriorityId = (json \ "defaultPriorityId").as[Option[Long]],
      defaultSeverityId = (json \ "defaultSeverityId").as[Option[Long]],
      defaultTypeId = (json \ "defaultTypeId").as[Option[Long]],
      defaultAssignee = (json \ "defaultAssignee").as[Option[Int]],
      dateCreated = (json \ "date_created").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

    def writes(obj: Project): JsValue = {

      val owner = obj.ownerId match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      }

      val prio = obj.defaultPriorityId match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      }

      val sev = obj.defaultSeverityId match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      }

      val ttype = obj.defaultTypeId match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      }

      val defAssign = obj.defaultAssignee match {
        case Some(id) => JsNumber(id)
        case None     => JsNull
      }

      val doc: Map[String,JsValue] = Map(
        "id"              -> JsNumber(obj.id.get),
        "workflowId"     -> JsNumber(obj.workflowId),
        "name"            -> JsString(obj.name),
        "key"             -> JsString(obj.key),
        "ownerId"        -> owner,
        "permission_schemeId" -> JsNumber(obj.permissionSchemeId),
        "defaultPriorityId" -> prio,
        "defaultSeverityId" -> sev,
        "defaultTypeId" -> ttype,
        "defaultAssignee" -> defAssign,
        "date_created"    -> JsString(dateFormatter.print(obj.dateCreated))
      )
      toJson(doc)
    }
  }

  /**
   * JSON conversion for TicketPriority
   */
  implicit object TicketPriorityFormat extends Format[TicketPriority] {

    def reads(json: JsValue): JsResult[TicketPriority] = JsSuccess(TicketPriority(
      id          = Id((json \ "id").as[Long]),
      name        = (json \ "name").as[String],
      color       = (json \ "color").as[String],
      position    = (json \ "position").as[Int],
      dateCreated = (json \ "date_created").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

    def writes(obj: TicketPriority): JsValue = {

      val doc: Map[String,JsValue] = Map(
        "id"            -> JsNumber(obj.id.get),
        "name"          -> JsString(obj.name),
        "nameI18N"      -> JsString(Messages(obj.name)),
        "color"         -> JsString(obj.color),
        "position"      -> JsNumber(obj.position),
        "date_created"  -> JsString(dateFormatter.print(obj.dateCreated))
      )
      toJson(doc)
    }
  }

  /**
   * JSON conversion for TicketSeverity
   */
  implicit object TicketSeverityFormat extends Format[TicketSeverity] {

    def reads(json: JsValue): JsResult[TicketSeverity] = JsSuccess(TicketSeverity(
      id          = Id((json \ "id").as[Long]),
      name        = (json \ "name").as[String],
      color       = (json \ "color").as[String],
      position    = (json \ "position").as[Int],
      dateCreated = (json \ "date_created").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

    def writes(obj: TicketSeverity): JsValue = {

      val doc: Map[String,JsValue] = Map(
        "id"            -> JsNumber(obj.id.get),
        "name"          -> JsString(obj.name),
        "nameI18N"      -> JsString(Messages(obj.name)),
        "color"         -> JsString(obj.color),
        "position"      -> JsNumber(obj.position),
        "date_created"  -> JsString(dateFormatter.print(obj.dateCreated))
      )
      toJson(doc)
    }
  }

  /**
   * JSON conversion for TicketType
   */
  implicit object TicketTypeFormat extends Format[TicketType] {

    def reads(json: JsValue): JsResult[TicketType] = JsSuccess(TicketType(
      id          = Id((json \ "id").as[Long]),
      name        = (json \ "name").as[String],
      color       = (json \ "color").as[String],
      dateCreated = (json \ "date_created").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

    def writes(obj: TicketType): JsValue = {

      val doc: Map[String,JsValue] = Map(
        "id"            -> JsNumber(obj.id.get),
        "name"          -> JsString(obj.name),
        "nameI18N"      -> JsString(Messages(obj.name)),
        "color"         -> JsString(obj.color),
        "dateCreated"   -> JsString(dateFormatter.print(obj.dateCreated))
      )
      toJson(doc)
    }
  }

  /**
   * JSON conversion for User
   */
  implicit object UserFormat extends Format[User] {

    def reads(json: JsValue): JsResult[User] = JsSuccess(User(
      id          = Id((json \ "id").as[Long]),
      username    = (json \ "username").as[String],
      password    = (json \ "password").as[String],
      realName    = (json \ "real_name").as[String],
      email       = (json \ "email").as[String],
      timezone    = (json \ "timezone").as[String],
      organization = (json \ "organization").as[Option[String]],
      location    = (json \ "location").as[Option[String]],
      title       = (json \ "title").as[Option[String]],
      url         = (json \ "url").as[Option[String]],
      dateCreated = (json \ "date_created").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

    def writes(obj: User): JsValue = {

      val doc: Map[String,JsValue] = Map(
        "id"              -> obj.id.map(i => JsNumber(i)).getOrElse(JsNull),
        "username"        -> JsString(obj.username),
        "password"        -> JsString(obj.password),
        "realName"        -> JsString(obj.realName),
        "realNameI18N"    -> JsString(Messages(obj.realName)),
        "email"           -> JsString(obj.email),
        "timezone"        -> JsString(obj.timezone),
        "organization"    -> optionStringtoJsValue(obj.organization),
        "location"        -> optionStringtoJsValue(obj.location),
        "title"           -> optionStringtoJsValue(obj.title),
        "url"             -> optionStringtoJsValue(obj.url),
        "dateCreated"     -> JsString(dateFormatter.print(obj.dateCreated))
      )
      toJson(doc)
    }
  }

  /**
   * JSON conversion for UserToken
   */
  implicit object UserTokenFormat extends Format[UserToken] {

    def reads(json: JsValue): JsResult[UserToken] = JsSuccess(UserToken(
      token   = Id((json \ "name").as[String]),
      userId  = (json \ "userId").as[Long],
      comment = (json \ "comment").as[Option[String]],
      dateCreated = (json \ "dateCreated").as[Option[String]].map({ d => dateFormatterUTC.parseDateTime(d) }).getOrElse(new DateTime())
    ))

    def writes(obj: UserToken): JsValue = {
      val doc: Map[String,JsValue] = Map(
        "token"         -> JsString(obj.token.get),
        "userId"        -> JsNumber(obj.userId),
        "comment"       -> optionStringtoJsValue(obj.comment),
        "dateCreated"  -> JsString(dateFormatter.print(obj.dateCreated))
      )
      toJson(doc)
    }
  }

  /**
   * JSON conversion for WorkflowStatus
   */
  implicit object WorkflowStatusFormat extends Format[WorkflowStatus] {

    def reads(json: JsValue): JsResult[WorkflowStatus] = JsSuccess(WorkflowStatus(
      id          = Id((json \ "id").as[Long]),
      workflowId  = (json \ "workflow_id").as[Long],
      statusId    = (json \ "status_id").as[Long],
      name        = (json \ "name").as[String],
      position    = (json \ "position").as[Int]
    ))

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
}