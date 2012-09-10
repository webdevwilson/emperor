package controllers

import anorm._
import chc._
import chc.JsonFormats._
import collection.JavaConversions._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Json._
import models._
import models.DefaultAssignee._
import models.TicketModel._
import org.clapper.markwrap._
import org.elasticsearch.search.facet.terms.longs.InternalLongTermsFacet
import org.elasticsearch.search.facet.terms.strings.InternalStringTermsFacet
import org.elasticsearch.search.sort._

import com.codahale.jerkson.Json._

object Ticket extends Controller with Secured {

  val mdParser = MarkWrap.parserFor(MarkupType.Markdown)

  val statusChangeForm = Form(
    mapping(
      "status_id" -> longNumber,
      "comment"   -> optional(text)
    )(models.StatusChange.apply)(models.StatusChange.unapply)
  )

  val assignForm = Form(
    mapping(
      "user_id" -> optional(longNumber),
      "comment" -> optional(text)
    )(models.Assignment.apply)(models.Assignment.unapply)
  )

  val resolveForm = Form(
    mapping(
      "resolution_id" -> longNumber,
      "comment" -> optional(text)
    )(models.Resolution.apply)(models.Resolution.unapply)
  )

  val commentForm = Form(
    mapping(
      "comment" -> nonEmptyText
    )(models.InitialComment.apply)(models.InitialComment.unapply)
  )

  val initialTicketForm = Form(
    mapping(
      "reporter_id" -> longNumber,
      "assignee_id" -> optional(longNumber),
      "project_id"  -> longNumber,
      "priority_id" -> longNumber,
      "severity_id" -> longNumber,
      "type_id"     -> longNumber,
      "position"    -> optional(longNumber),
      "summary"     -> nonEmptyText,
      "description" -> optional(text)
    )(models.InitialTicket.apply)(models.InitialTicket.unapply)
  )

  val ticketForm = Form(
    mapping(
      "ticket_id"     -> ignored(NotAssigned:Pk[String]),
      "reporter_id"   -> longNumber,
      "assignee_id"   -> optional(longNumber),
      "attention_id"  -> optional(longNumber),
      "project_id"    -> longNumber,
      "priority_id"   -> longNumber,
      "resolution_id" -> optional(longNumber),
      "proposed_resolution_id" -> optional(longNumber),
      "severity_id"   -> longNumber,
      "type_id"       -> longNumber,
      "position"      -> optional(longNumber),
      "summary"       -> nonEmptyText,
      "description"   -> optional(text)
    )(models.EditTicket.apply)(models.EditTicket.unapply)
  )

  def doResolve(ticketId: String) = IsAuthenticated { implicit request =>

    val ticket = TicketModel.getFullById(ticketId)

    ticket match {
      case Some(value) => {
        resolveForm.bindFromRequest.fold(
          errors => {
            BadRequest(views.html.ticket.error(request))
          }, {
            case resolution: models.Resolution => {
              val nt = TicketModel.resolve(ticketId = ticketId, userId = request.session.get("userId").get.toLong, resolutionId = resolution.resolutionId,  comment = resolution.comment)
              Redirect(routes.Ticket.item("comments", ticketId)).flashing("success" -> "ticket.success.resolution")
            }
          }
        )
      }
      case None => BadRequest(views.html.ticket.error(request))
    }
  }

  def doUnResolve(ticketId: String) = IsAuthenticated { implicit request =>

    val ticket = TicketModel.getFullById(ticketId)

    ticket match {
      case Some(value) => {
        commentForm.bindFromRequest.fold(
          errors => {
            BadRequest(views.html.ticket.error(request))
          }, {
            case unresolution: models.InitialComment => {
              val nt = TicketModel.unresolve(ticketId = ticketId, userId = request.session.get("userId").get.toLong, comment = Some(unresolution.comment))
              Redirect(routes.Ticket.item("comments", ticketId)).flashing("success" -> "ticket.success.unresolution")
            }
          }
        )
      }
      case None => BadRequest(views.html.ticket.error(request))
    }
  }

  def doAssign(ticketId: String) = IsAuthenticated { implicit request =>

    val ticket = TicketModel.getFullById(ticketId)

    ticket match {
      case Some(value) => {
        assignForm.bindFromRequest.fold(
          errors => {
            BadRequest(views.html.ticket.error(request))
          }, {
            case assignment: models.Assignment => {
              val nt = TicketModel.assign(ticketId = ticketId, userId = request.session.get("userId").get.toLong, assigneeId = assignment.userId, comment = assignment.comment)
              Redirect(routes.Ticket.item("comments", ticketId)).flashing("success" -> "ticket.assignment.success")
            }
          }
        )
      }
      case None => BadRequest(views.html.ticket.error(request))
    }
  }

  def status(ticketId: String) = IsAuthenticated { implicit request =>

    val ticket = TicketModel.getFullById(ticketId)

    ticket match {
      case Some(value) => {
        statusChangeForm.bindFromRequest.fold(
          errors => {
            val prevStatus = WorkflowModel.getPreviousStatus(value.workflowStatusId)
            val nextStatus = WorkflowModel.getNextStatus(value.workflowStatusId)
            Redirect(routes.Ticket.item("comments", ticketId)).flashing("error" -> "ticket.error.status")
          }, {
            case statusChange: models.StatusChange => {
              TicketModel.changeStatus(ticketId, statusChange.statusId, request.session.get("userId").get.toLong, comment = statusChange.comment)
              Redirect(routes.Ticket.item("comments", ticketId)).flashing("success" -> "ticket.success.status")
            }
          }
        )
      }
      case None => NotFound
    }
  }

  def add = IsAuthenticated { implicit request =>

    initialTicketForm.bindFromRequest.fold(
      errors => {
        // Should be i18ned in the view
        val users = UserModel.getAll.map { x => (x.id.get.toString -> x.realName) }
        val projs = ProjectModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val assignees = ("" -> Messages("ticket.unassigned")) +: users

        BadRequest(views.html.ticket.create(errors, users, assignees, projs, ttypes, prios, sevs))
      },
      value => {
        val ticket = TicketModel.create(userId = request.session.get("userId").get.toLong, ticket = value)
        ticket match {
          case Some(t) => {
            SearchModel.indexTicket(ticket.get)
            Redirect(routes.Ticket.item("comments", t.ticketId)).flashing("success" -> "ticket.add.success")
          }
          case None => Redirect(routes.Ticket.item("comments", ticket.get.ticketId)).flashing("error" -> "ticket.add.failure")
        }
      }
    )
  }

  def comment(ticketId: String) = IsAuthenticated { implicit request =>

    commentForm.bindFromRequest.fold(
      errors => {
        Redirect(routes.Ticket.item("comments", ticketId)).flashing("error" -> "ticket.comment.invalid")
      },
      value => {
        val comm = TicketModel.addComment(ticketId, request.session.get("userId").get.toLong, value.comment)
        SearchModel.indexComment(comm.get)

        Redirect(routes.Ticket.item("comments", ticketId)).flashing("success" -> "ticket.comment.added")
      }
    )
  }

  /**
   * Display a form for creating a ticket.
   * If we received a projectId then we will default the form to the appropriate
   * settings for the provided project.
   */
  def create(projectId: Option[Long]) = IsAuthenticated { implicit request =>

    // Should be i18ned in the view
    val projs = ProjectModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val users = UserModel.getAll.map { x => (x.id.get.toString -> x.realName) }

    // The worst case scenario, just the user id
    val startTicket = InitialTicket(
      reporterId = request.session.get("userId").get.toLong,
      assigneeId = None, // XXX need to fix this
      projectId = 0,
      priorityId = 0,
      severityId = 0,
      typeId = 0,
      position = None,
      summary = "",
      description = None
    )

    // XXX No, no. This is what's happening if there's no project? No project
    // should be an error. Grrr.
    val finalTicket = projectId match {
      case Some(pid) => {
        val maybeProject = ProjectModel.getById(pid)

        maybeProject match {
          case Some(project) => {
            // If we got a project then copy the "default" ticket and modify
            // the appropriate settings for the project's defaults.
            startTicket.copy(
              assigneeId = project.defaultAssignee match {
                // Choose the appropriate default assignee based on the
                // strategy set on the project.
                case Some(da) if da == Def_Assign_Owner.id => project.ownerId
                case _ => None
              },
              projectId = pid,
              priorityId = project.defaultPriorityId.getOrElse(0),
              severityId = project.defaultSeverityId.getOrElse(0),
              typeId = project.defaultTypeId.getOrElse(0)
            )
          }
          case None => startTicket
        }
      }
      case None => startTicket
    }

    val assignees = projectId match {
      case Some(project) => UserModel.getAssignable(projectId = project).map { x => (x.id.getOrElse("").toString -> x.realName) }
      case None => UserModel.getAll.map { x => (x.id.get.toString -> x.realName) }
    }

    val defaultedForm = initialTicketForm.fill(finalTicket)

    Ok(views.html.ticket.create(defaultedForm, users, assignees, projs, ttypes, prios, sevs)(request))
  }

  def edit(ticketId: String) = IsAuthenticated { implicit request =>

    val users = UserModel.getAll.map { x => (x.id.get.toString -> x.realName) }
    val ticket = TicketModel.getById(ticketId)
    val projs = ProjectModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val assignees = ("" -> Messages("ticket.unassigned")) +: users
    val attentions = ("" -> Messages("ticket.unassigned")) +: users

    ticket match {
      case Some(value) => Ok(views.html.ticket.edit(ticketId, ticketForm.fill(value), users, assignees, attentions, projs, ttypes, prios, sevs)(request))
      case None => NotFound
    }
  }

  def item(tab: String = "comments", ticketId: String, page: Int, count: Int, query: String) = IsAuthenticated { implicit request =>

    val maybeTicket = TicketModel.getFullById(ticketId)

    maybeTicket match {
      case Some(ticket) => {

        val prevStatus = WorkflowModel.getPreviousStatus(ticket.workflowStatusId)
        val nextStatus = WorkflowModel.getNextStatus(ticket.workflowStatusId)

        val links = TicketModel.getLinks(ticketId).groupBy( l =>
          if(l.childId == ticketId) {
            l.typeName + "_INVERT"
          } else {
            l.typeName
          }
        )

        val resolutions = TicketResolutionModel.getAll.map { reso => (reso.id.get.toString -> Messages(reso.name)) }

        val assignees = UserModel.getAssignable(projectId = ticket.project.id).map { user => (user.id.getOrElse("").toString -> user.realName) }

        val ltypes = TicketLinkTypeModel.getAll

        tab match {
          case "history"  => {

            val changeFilters = Map("ticket_id" -> Seq(ticketId))

            // XXX Different page & count (100?)
            val changeRes = SearchModel.searchChange(
              page, 100, "", changeFilters
            )

            val history = Page(changeRes.hits.hits, page, count, changeRes.hits.totalHits)

            val historyFacets = changeRes.facets.facets.map { facet =>
              facet match {
                case t: InternalStringTermsFacet => t
              }
            } //filter { f => f.entries.size > 1 }

            Ok(views.html.ticket.history(
              ticket = ticket,
              links = links,
              markdown = mdParser,
              assignees = assignees,
              resolutions = resolutions,
              resolveForm = resolveForm,
              assignForm = assignForm.fill(Assignment(ticket.assignee.id, None)),
              commentForm = commentForm,
              history = history,
              historyFacets = historyFacets,
              previousStatus = prevStatus,
              nextStatus = nextStatus,
              linkTypes = ltypes
            )(request))
          }
          case _ => {

            val commFilters = Map("ticket_id" -> Seq(ticketId))

            // XXX Different page & count
            val commRes = SearchModel.searchComment(
              page, count, query, commFilters, Seq("date_created" -> SortOrder.ASC)
            )
            val comments = Page(commRes.hits.hits, page, count, commRes.hits.totalHits)

            val commFacets = commRes.facets.facets.map { facet =>
              facet match {
                case t: InternalLongTermsFacet => t
              }
            } filter { f => f.entries.size > 1 }

            Ok(views.html.ticket.comments(
              ticket = ticket,
              links = links,
              markdown = mdParser,
              assignees = assignees,
              resolutions = resolutions,
              resolveForm = resolveForm,
              assignForm = assignForm.fill(Assignment(ticket.assignee.id, None)),
              commentForm = commentForm,
              comments = comments,
              commFacets = commFacets,
              previousStatus = prevStatus,
              nextStatus = nextStatus,
              linkTypes = ltypes
            )(request))
          }
        }
      }
      case None => NotFound
    }
  }

  def update(ticketId: String) = IsAuthenticated { implicit request =>

    ticketForm.bindFromRequest.fold(
      errors => {
        val users = UserModel.getAll.map { x => (x.id.get.toString -> x.realName) }
        val projs = ProjectModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val assignees = ("" -> Messages("ticket.unassigned")) +: users
        val attentions = ("" -> Messages("ticket.unassigned")) +: users

        BadRequest(views.html.ticket.edit(ticketId, errors, users, assignees, attentions, projs, ttypes, prios, sevs))
      },
      value => {
        TicketModel.update(request.session.get("userId").get.toLong, ticketId, value)
        SearchModel.indexTicket(TicketModel.getFullById(ticketId).get)
        Redirect(routes.Ticket.item("comments", ticketId)).flashing("success" -> "ticket.edit.success")
      }
    )
  }
}
