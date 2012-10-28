package controllers

import anorm._
import emp.util.Pagination.Page
import emp.util.Search._
import emp.JsonFormats._
import collection.JavaConversions._
import java.util.Date
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Json._
import emp.util.Search._
import models._
import models.DefaultAssignee._
import models.TicketModel._
import org.elasticsearch.search.facet.terms.longs.InternalLongTermsFacet
import org.elasticsearch.search.facet.terms.strings.InternalStringTermsFacet
import org.elasticsearch.search.sort._

import com.codahale.jerkson.Json._

object Ticket extends Controller with Secured {

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
      "comment"       -> optional(text)
    )(models.Resolution.apply)(models.Resolution.unapply)
  )

  val commentForm = Form(
    mapping(
      "id"      -> ignored(NotAssigned:Pk[Long]),
      "user_id" -> ignored[Long](0.toLong),
      "username"-> ignored[String](""),
      "realname"-> ignored[String](""),
      "ticket_id" -> ignored[String](""),
      "content" -> nonEmptyText,
      "date_created" -> ignored[Date](new Date())
    )(models.Comment.apply)(models.Comment.unapply)
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

  def doResolve(ticketId: String) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_RESOLVE") { implicit request =>

    val ticket = TicketModel.getFullById(ticketId)

    ticket match {
      case Some(value) => {
        resolveForm.bindFromRequest.fold(
          errors => {
            BadRequest(views.html.ticket.error(request))
          }, {
            case resolution: models.Resolution => {
              val nt = TicketModel.resolve(ticketId = ticketId, userId = request.user.id.get, resolutionId = resolution.resolutionId,  comment = resolution.comment)
              Redirect(routes.Ticket.item("comments", ticketId)).flashing("success" -> "ticket.success.resolution")
            }
          }
        )
      }
      case None => BadRequest(views.html.ticket.error(request))
    }
  }

  def doUnResolve(ticketId: String) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_RESOLVE") { implicit request =>

    val ticket = TicketModel.getFullById(ticketId)

    ticket match {
      case Some(value) => {
        commentForm.bindFromRequest.fold(
          errors => {
            BadRequest(views.html.ticket.error(request))
          }, {
            case unresolution: models.Comment => {
              val nt = TicketModel.unresolve(ticketId = ticketId, userId = request.user.id.get, comment = Some(unresolution.content))
              Redirect(routes.Ticket.item("comments", ticketId)).flashing("success" -> "ticket.success.unresolution")
            }
          }
        )
      }
      case None => BadRequest(views.html.ticket.error(request))
    }
  }

  def doAssign(ticketId: String) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_EDIT") { implicit request =>

    val ticket = TicketModel.getFullById(ticketId)

    ticket match {
      case Some(value) => {
        assignForm.bindFromRequest.fold(
          errors => {
            BadRequest(views.html.ticket.error(request))
          }, {
            case assignment: models.Assignment => {
              val nt = TicketModel.assign(ticketId = ticketId, userId = request.user.id.get, assigneeId = assignment.userId, comment = assignment.comment)
              Redirect(routes.Ticket.item("comments", ticketId)).flashing("success" -> "ticket.assignment.success")
            }
          }
        )
      }
      case None => BadRequest(views.html.ticket.error(request))
    }
  }

  def status(ticketId: String) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_EDIT") { implicit request =>

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
              TicketModel.changeStatus(ticketId, statusChange.statusId, request.user.id.get, comment = statusChange.comment)
              Redirect(routes.Ticket.item("comments", ticketId)).flashing("success" -> "ticket.success.status")
            }
          }
        )
      }
      case None => NotFound
    }
  }

  def add = IsAuthenticated() { implicit request =>

    initialTicketForm.bindFromRequest.fold(
      errors => {
        // Should be i18ned in the view
        val projs = ProjectModel.getAll(userId = request.user.id.get).map { x => (x.id.get.toString -> Messages(x.name)) }
        val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val projId = errors("project_id").value.map({ pid => if(pid.isEmpty) None else Some(pid.toLong) }).getOrElse(None)
        val assignees = UserModel.getAssignable(projectId = projId).map { x => (x.id.getOrElse("").toString -> Messages(x.realName)) }
        val finalProjects = if(errors("project_id").value.isEmpty) projs else ("" -> Messages("project.choose")) +: projs
        BadRequest(views.html.ticket.create(errors, assignees, assignees, finalProjects, ttypes, prios, sevs))
      },
      value => {
        val maybeCan = PermissionSchemeModel.hasPermission(
          projectId = value.projectId, perm = "PERM_TICKET_CREATE", userId = request.user.id.get
        )
        // Check the permission and only execute the create if it's defined,
        // otherwise return a redurect and tell 'em no.
        maybeCan.map({ perm =>
          val ticket = TicketModel.create(userId = request.user.id.get, ticket = value)
          ticket match {
            case Some(t) => {
              SearchModel.indexTicket(ticket.get)
              Redirect(routes.Ticket.item("comments", t.ticketId)).flashing("success" -> "ticket.add.success")
            }
            case None => Redirect(routes.Ticket.item("comments", ticket.get.ticketId)).flashing("error" -> "ticket.add.failure")
          }
        }).getOrElse(Redirect(routes.Core.index).flashing("error" -> "auth.notauthorized"))
      }
    )
  }

  def comment(ticketId: String) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_COMMENT") { implicit request =>

    commentForm.bindFromRequest.fold(
      errors => {
        Redirect(routes.Ticket.item("comments", ticketId)).flashing("error" -> "ticket.comment.invalid")
      },
      value => {
        val comm = TicketModel.addComment(ticketId, request.user.id.get, value.content)
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
  def create(projectId: Option[Long]) = IsAuthenticated() { implicit request =>

    // Should be i18ned in the view
    val projs = ProjectModel.getAll(userId = request.user.id.get).map { x => (x.id.get.toString -> Messages(x.name)) }
    val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

    // The worst case scenario, just the user id
    val startTicket = InitialTicket(
      reporterId = request.user.id.get,
      assigneeId = None,
      projectId = 0,
      priorityId = 0,
      severityId = 0,
      typeId = 0,
      position = None,
      summary = "",
      description = None
    )

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

    val assignees = UserModel.getAssignable(projectId = projectId).map { x => (x.id.getOrElse("").toString -> Messages(x.realName)) }

    val defaultedForm = initialTicketForm.fill(finalTicket)

    val finalProjects = projectId.map({ pid => projs }).getOrElse(("" -> Messages("project.choose")) +: projs)

    Ok(views.html.ticket.create(defaultedForm, assignees, assignees, finalProjects, ttypes, prios, sevs)(request))
  }

  def edit(ticketId: String) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_EDIT") { implicit request =>

    val maybeTicket = TicketModel.getById(ticketId)

    maybeTicket match {
      case Some(ticket) => {
        val projs = ProjectModel.getAll(userId = request.user.id.get).map { x => (x.id.get.toString -> Messages(x.name)) }
        val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val assignees = UserModel.getAssignable(projectId = Some(ticket.projectId)).map { x => (x.id.getOrElse("").toString -> Messages(x.realName)) }
        Ok(views.html.ticket.edit(ticketId, ticketForm.fill(ticket), assignees, assignees, assignees, projs, ttypes, prios, sevs)(request))
      }
      case None => NotFound
    }
  }

  def item(tab: String = "comments", ticketId: String, page: Int = 1, count: Int= 10, query: String = "*") = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_PROJECT_BROWSE") { implicit request =>

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

        val assignees = UserModel.getAssignable(projectId = Some(ticket.project.id)).map { user => (user.id.getOrElse("").toString -> Messages(user.realName)) }

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
            } // filter { f => f.entries.size > 1 } // only show facets with > 1 item

            Ok(views.html.ticket.history(
              ticket = ticket,
              links = links,
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

            val q = SearchQuery(
              userId = request.user.id.get, page = page,
              count = count, query = query, filters = commFilters
            )
            val commRes = SearchModel.searchComment(q)
            // val comments = Page(commRes.hits.hits, page, count, commRes.hits.totalHits)

            // val commFacets = commRes.facets.facets.map { facet =>
            //   facet match {
            //     case t: InternalLongTermsFacet => t
            //   }
            // } filter { f => f.entries.size > 1 }

            Ok(views.html.ticket.comments(
              ticket = ticket,
              links = links,
              assignees = assignees,
              resolutions = resolutions,
              resolveForm = resolveForm,
              assignForm = assignForm.fill(Assignment(ticket.assignee.id, None)),
              commentForm = commentForm,
              comments = commRes,
              // commFacets = commFacets,
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

  def update(ticketId: String) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_EDIT") { implicit request =>

    ticketForm.bindFromRequest.fold(
      errors => {
        val projs = ProjectModel.getAll(userId = request.user.id.get).map { x => (x.id.get.toString -> Messages(x.name)) }
        val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
        val projId = errors("project_id").value.get.toLong
        val assignees = UserModel.getAssignable(projectId = Some(projId)).map { user => (user.id.getOrElse("").toString -> Messages(user.realName)) }

        BadRequest(views.html.ticket.edit(ticketId, errors, assignees, assignees, assignees, projs, ttypes, prios, sevs))
      },
      value => {
        // XXX validate assignability
        TicketModel.update(request.user.id.get, ticketId, value)
        SearchModel.indexTicket(TicketModel.getFullById(ticketId).get)
        Redirect(routes.Ticket.item("comments", ticketId)).flashing("success" -> "ticket.edit.success")
      }
    )
  }
}
