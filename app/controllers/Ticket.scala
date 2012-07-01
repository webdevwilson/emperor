package controllers

import anorm._
import chc._
import collection.JavaConversions._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc._
import play.api.libs.json.Json
import play.api.libs.json.Json._
import models._
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
      "content" -> optional(text)
    )(models.Resolution.apply)(models.Resolution.unapply)
  )

  val commentForm = Form(
    mapping(
      "content" -> nonEmptyText
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
      "ticket_id"     -> text,
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

  def resolve(ticketId: String) = IsAuthenticated { implicit request =>
    
    val ticket = TicketModel.getFullById(ticketId)

    val resolutions = TicketResolutionModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
    
    ticket match {
      case Some(value) => Ok(views.html.ticket.resolve(ticketId, value, resolutions, resolveForm)(request))
      case None => BadRequest(views.html.ticket.error(request))
    }
  }

  def unresolve(ticketId: String) = IsAuthenticated { implicit request =>
    
    val ticket = TicketModel.getFullById(ticketId)

    ticket match {
      case Some(value) => Ok(views.html.ticket.unresolve(ticketId, value, commentForm)(request))
      case None => BadRequest(views.html.ticket.error(request))
    }
  }

  def doResolve(ticketId: String) = IsAuthenticated { implicit request =>
    
    val ticket = TicketModel.getFullById(ticketId)
    
    ticket match {
      case Some(value) => {
        resolveForm.bindFromRequest.fold(
          errors => {
            BadRequest(views.html.ticket.error(request))
          }, {
            case resolution: models.Resolution => {
              TicketModel.resolve(ticketId = ticketId, userId = request.session.get("userId").get.toLong, resolutionId = resolution.resolutionId)
              Redirect(routes.Ticket.item("history", ticketId)).flashing("success" -> "ticket.success.resolution")
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
            println(errors)
            BadRequest(views.html.ticket.error(request))
          }, {
            case resolution: models.InitialComment => {
              TicketModel.unresolve(ticketId = ticketId, userId = request.session.get("userId").get.toLong)
              Redirect(routes.Ticket.item("history", ticketId)).flashing("success" -> "ticket.success.unresolution")
            }
          }
        )
      }
      case None => BadRequest(views.html.ticket.error(request))
    }
  }

  def newStatus(ticketId: String, statusId: Long) = IsAuthenticated { implicit request =>
    
    val ticket = TicketModel.getFullById(ticketId)
    val newStatus = WorkflowModel.getStatusById(statusId)
    // XXX some sort of check too many gets!
    
    // XXX form errors?
    
    ticket match {
      case Some(value) => {
        newStatus match {
          case Some(status) => Ok(views.html.ticket.newstatus(ticketId, value, status, commentForm)(request))
          case None => BadRequest(views.html.ticket.error(request))
        }
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
            // XXX page!
            Redirect(routes.Ticket.item("comments", ticketId)).flashing("error" -> "ticket.error.status")
          }, {
            case statusChange: models.StatusChange => {
              TicketModel.changeStatus(ticketId, statusChange.statusId, request.session.get("userId").get.toLong)
              statusChange.comment match {
                case Some(content) => {
                  val comm = TicketModel.addComment(ticketId, request.session.get("userId").get.toLong, content)
                  SearchModel.indexComment(comm.get) // XXX actors? handle None!
                }
                case None => //
              }
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
            SearchModel.indexTicket(TicketModel.getFullById(t.ticketId).get) // XXX actors, elsewhere?!!
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
        Redirect(routes.Ticket.item("comment", ticketId)).flashing("error" -> "ticket.comment.invalid")
      },
      value => {
        val comm = TicketModel.addComment(ticketId, request.session.get("userId").get.toLong, value.content)
        SearchModel.indexComment(comm.get) // XXX actors? handle None!
        Redirect(routes.Ticket.item("comments", ticketId)).flashing("success" -> "ticket.comment.added")
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
    // XXX Should really match this here and return if it's not found
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

  def item(tab: String, ticketId: String, page: Int, count: Int, query: String) = IsAuthenticated { implicit request =>
    
    val ticket = TicketModel.getFullById(ticketId)

    ticket match {
      case Some(value) => {

        val prevStatus = WorkflowModel.getPreviousStatus(value.workflowStatusId)
        val nextStatus = WorkflowModel.getNextStatus(value.workflowStatusId)
        
        tab match {
          case "history"  => {

            // var filters = request.queryString.map { param =>
            //   val p = param._1
            //   p match {
            //     case "user_id" => p -> request.queryString.get(p).get
            //   }
            // }
            // filters += "ticket_id" -> Seq(ticketId.toString)
            val filters = Map("ticket_id" -> Seq(ticketId.toString))

            val response = SearchModel.searchChange(
              page, count, "", Map("ticket_id" -> Seq(ticketId.toString))
            )
            val pager = Page(response.hits.hits, page, count, response.hits.totalHits)

            val changeFacets = response.facets.facets.map { facet =>
              facet match {
                case t: InternalStringTermsFacet => t
              }
            } //filter { f => f.entries.size > 1 }

            Ok(views.html.ticket.history(
              ticket = value,
              markdown = mdParser,
              prevStatus = prevStatus,
              nextStatus = nextStatus,
              history = pager,
              changeFacets = changeFacets,
              response = response
            )(request))
          }
          case _ => {

            // var filters: Map[String,Seq[String]]= request.queryString.map { param =>
            //   val p = param._1
            //   p match {
            //     case "user_id" => p -> request.queryString.get(p).get
            //   }
            // }
            // filters += "ticket_id" -> Seq(ticketId.toString)
            val filters = Map("ticket_id" -> Seq(ticketId.toString))

            val response = SearchModel.searchComment(
              page, count, query, filters
            )
            val pager = Page(response.hits.hits, page, count, response.hits.totalHits)

            val termfacets = response.facets.facets.map { facet =>
              facet match {
                case t: InternalLongTermsFacet => t
              }
            } filter { f => f.entries.size > 1 }

            Ok(views.html.ticket.comments(
              ticket = value,
              markdown = mdParser,
              prevStatus = prevStatus,
              nextStatus = nextStatus,
              commentForm = commentForm,
              comments = pager,
              facets = termfacets,
              response = response
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
        SearchModel.indexTicket(TicketModel.getFullById(ticketId).get) // XXX actors, elsewhere?!!
        Redirect(routes.Ticket.item("history", ticketId)).flashing("success" -> "ticket.edit.success")
      }
    )
  }
}