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
import play.api.libs.json.Json
import play.api.libs.json.Json._
import models._
import models.TicketModel._
import org.clapper.markwrap._
import org.elasticsearch.search.facet.terms.longs.InternalLongTermsFacet
import org.elasticsearch.search.facet.terms.strings.InternalStringTermsFacet

import com.codahale.jerkson.Json._

object Ticket extends Controller with Secured {

  val mdParser = MarkWrap.parserFor(MarkupType.Markdown)

  val statusChangeForm = Form(
    mapping(
      "status_id" -> longNumber,
      "comment"   -> optional(text)
    )(models.StatusChange.apply)(models.StatusChange.unapply)
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
              SearchModel.indexTicket(ticket = nt)
              Redirect(routes.Ticket.item(ticketId)).flashing("success" -> "ticket.success.resolution")
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
            case resolution: models.InitialComment => {
              val nt = TicketModel.unresolve(ticketId = ticketId, userId = request.session.get("userId").get.toLong, comment = Some(resolution.comment))
              SearchModel.indexTicket(ticket = nt)
              Redirect(routes.Ticket.item(ticketId)).flashing("success" -> "ticket.success.unresolution")
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
            Redirect(routes.Ticket.item(ticketId)).flashing("error" -> "ticket.error.status")
          }, {
            case statusChange: models.StatusChange => {
              TicketModel.changeStatus(ticketId, statusChange.statusId, request.session.get("userId").get.toLong, comment = statusChange.comment)
              Redirect(routes.Ticket.item(ticketId)).flashing("success" -> "ticket.success.status")
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
            Redirect(routes.Ticket.item(t.ticketId)).flashing("success" -> "ticket.add.success")
          }
          case None => Redirect(routes.Ticket.item(ticket.get.ticketId)).flashing("error" -> "ticket.add.failure")
        }
      }
    )
  }

  def comment(ticketId: String) = IsAuthenticated { implicit request =>

    commentForm.bindFromRequest.fold(
      errors => {
        Redirect(routes.Ticket.item(ticketId)).flashing("error" -> "ticket.comment.invalid")
      },
      value => {
        val comm = TicketModel.addComment(ticketId, request.session.get("userId").get.toLong, value.comment)
        SearchModel.indexComment(comm.get)
        Redirect(routes.Ticket.item(ticketId)).flashing("success" -> "ticket.comment.added")
      }
    )
  }

  def create = IsAuthenticated { implicit request =>

    // Should be i18ned in the view
    val users = UserModel.getAll.map { x => (x.id.get.toString -> x.realName) }
    val projs = ProjectModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    val assignees = ("" -> Messages("ticket.unassigned")) +: users

    Ok(views.html.ticket.create(initialTicketForm, users, assignees, projs, ttypes, prios, sevs)(request))
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

  def item(ticketId: String, page: Int, count: Int, query: String) = IsAuthenticated { implicit request =>

    val ticket = TicketModel.getFullById(ticketId)

    ticket match {
      case Some(value) => {

        val prevStatus = WorkflowModel.getPreviousStatus(value.workflowStatusId)
        val nextStatus = WorkflowModel.getNextStatus(value.workflowStatusId)

        val resolutions = TicketResolutionModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }

        val commFilters = Map("ticket_id" -> Seq(ticketId))

        val links = TicketModel.getLinks(ticketId)

        // XXX Different page & count
        val commRes = SearchModel.searchComment(
          page, count, query, commFilters
        )
        val comments = Page(commRes.hits.hits, page, count, commRes.hits.totalHits)

        val commFacets = commRes.facets.facets.map { facet =>
          facet match {
            case t: InternalLongTermsFacet => t
          }
        } filter { f => f.entries.size > 1 }

        val changeFilters = Map("ticket_id" -> Seq(ticketId))

        // XXX Different page & count
        val changeRes = SearchModel.searchChange(
          page, count, "", changeFilters
        )
        val history = Page(changeRes.hits.hits, page, count, changeRes.hits.totalHits)

        val historyFacets = changeRes.facets.facets.map { facet =>
          facet match {
            case t: InternalStringTermsFacet => t
          }
        } //filter { f => f.entries.size > 1 }

        Ok(views.html.ticket.item(
          ticket = value,
          markdown = mdParser,
          prevStatus = prevStatus,
          nextStatus = nextStatus,
          resolutions = resolutions,
          resolveForm = resolveForm,
          commentForm = commentForm,
          links = links,
          comments = comments,
          commFacets = commFacets,
          history = history,
          historyFacets = historyFacets
        )(request))
      }
      case None => NotFound
    }
  }

  def link(typeId: Long, parentId: String, childId: String) = IsAuthenticated { implicit request =>

    val link = if(parentId == childId) {
      None
    } else {
      TicketModel.link(linkTypeId = typeId, parentId = parentId, childId = childId)
    }

    link match {
      case Some(query) => Ok(Messages("ticket.linker.success"))
      case None => Accepted(Messages("ticket.linker.maybe"))
    }
  }

  def linker = IsAuthenticated { implicit request =>

    val ltypes = TicketLinkTypeModel.getAll

    val ticketId = request.session.get("link_ticket")
    ticketId match {
      case Some(id) => Ok(views.html.util.linker(id, ltypes))
      case None => Ok("")
    }
  }

  def startLink(ticketId: String) = IsAuthenticated { implicit request =>

    Ok(Messages("ticket.linker.start", ticketId)).withSession(session + ("link_ticket" -> ticketId))
  }

  def stopLink = IsAuthenticated { implicit request =>

    Ok(Messages("ticket.linker.stop")).withSession(session - "link_ticket")
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
        Redirect(routes.Ticket.item(ticketId)).flashing("success" -> "ticket.edit.success")
      }
    )
  }
}
