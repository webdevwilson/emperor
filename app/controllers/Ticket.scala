package controllers

import anorm._
import emp.util.Pagination.Page
import emp.util.Search._
import emp.JsonFormats._
import collection.JavaConversions._
import org.joda.time.DateTime
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
import org.joda.time.DateTime
import scala.math._

object Ticket extends Controller with Secured {

  val linkForm = Form(
    mapping(
      "link_type_id"-> longNumber,
      "ticket" -> play.api.data.Forms.list(nonEmptyText),
      "comment"     -> optional(text)
    )(models.MakeLink.apply)(models.MakeLink.unapply)
  )

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
      "ctype"   -> ignored[String]("comment"),
      "user_id" -> ignored[Long](0.toLong),
      "username"-> ignored[String](""),
      "realname"-> ignored[String](""),
      "ticket_id" -> ignored[String](""),
      "content" -> nonEmptyText,
      "date_created" -> ignored[DateTime](new DateTime())
    )(models.Comment.apply)(models.Comment.unapply)
  )

  val ticketForm = Form(
    mapping(
      "projectId"  -> longNumber,
      "typeId"     -> longNumber,
      "priorityId" -> longNumber,
      "severityId" -> longNumber,
      "summary"     -> nonEmptyText,
      "description" -> optional(text),
      "assigneeId" -> optional(longNumber),
      "position"    -> optional(longNumber)
    )(models.NewTicket.apply)(models.NewTicket.unapply)
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

  def add = IsAuthenticated() { implicit request =>

    ticketForm.bindFromRequest.fold(
      errors => {
        val projs = Json.toJson(ProjectModel.getAll(userId = request.user.id.get)).toString
        val ttypes = Json.toJson(TicketTypeModel.getAll).toString
        val prios = Json.toJson(TicketPriorityModel.getAll).toString
        val sevs = Json.toJson(TicketSeverityModel.getAll).toString
        val projId = errors("project_id").value.map({ pid => if(pid.isEmpty) None else Some(pid.toLong) }).getOrElse(None)
        val assignees = Json.toJson(UserModel.getAssignable(projectId = projId)).toString
        BadRequest(views.html.ticket.create(errors, assignees, projs, ttypes, prios, sevs, "{}"))
      },
      value => {
        val maybeCan = PermissionSchemeModel.hasPermission(
          projectId = value.projectId, perm = "PERM_TICKET_CREATE", userId = request.user.id.get
        )
        // Check the permission and only execute the create if it's defined,
        // otherwise return a redurect and tell 'em no.
        maybeCan.map({ perm =>
          val ticket = TicketModel.create(
            userId = request.user.id.get, projectId = value.projectId, typeId = value.typeId, priorityId = value.priorityId,
            severityId = value.severityId, summary = value.summary, description = value.description,
            assigneeId = value.assigneeId, position = value.position
          )
          ticket match {
            case Some(t) => {
              Redirect(routes.Ticket.item("comments", t.ticketId)).flashing("success" -> "ticket.add.success")
            }
            case None => Redirect(routes.Ticket.item("comments", ticket.get.ticketId)).flashing("error" -> "ticket.add.failure")
          }
        }).getOrElse(Redirect(routes.Core.index()).flashing("error" -> "auth.notauthorized"))
      }
    )
  }

  def change(ticketId: String, statusId: Long) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_EDIT") { implicit request =>

    TicketStatusModel.getById(statusId).map({ status =>
      TicketModel.getFullById(ticketId).map({ ticket =>
        Ok(views.html.ticket.change(ticket, status, commentForm))
      }).getOrElse(NotFound)
    }).getOrElse(NotFound)
  }

  def comment(ticketId: String) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_COMMENT") { implicit request =>

    commentForm.bindFromRequest.fold(
      errors => {
        Redirect(routes.Ticket.item("comments", ticketId)).flashing("error" -> "ticket.comment.invalid")
      },
      value => {
        val comm = TicketModel.addComment(ticketId, value.ctype, request.user.id.get, value.content)

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
    val projs = Json.toJson(ProjectModel.getAll(userId = request.user.id.get)).toString
    val ttypes = Json.toJson(TicketTypeModel.getAll).toString
    val prios = Json.toJson(TicketPriorityModel.getAll).toString
    val sevs = Json.toJson(TicketSeverityModel.getAll).toString

    // The worst case scenario, just the user id
    val startTicket = models.NewTicket(
      projectId = 0,
      priorityId = 0,
      severityId = 0,
      typeId = 0,
      summary = "",
      description = None
    )

    val maybeProject: Option[Project] = projectId.flatMap({ id => ProjectModel.getById(id) })

    val assignees = Json.toJson(UserModel.getAssignable(projectId = projectId)).toString

    val pj: String = maybeProject.map({ p => Json.toJson(p).toString }).getOrElse("{}")

    Ok(views.html.ticket.create(ticketForm, assignees, projs, ttypes, prios, sevs, pj)(request))
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

        val wf = WorkflowModel.getForTicket(ticket.ticketId)
        val wfs = WorkflowModel.getStatuses(wf.get.id.get)

        val resolutions = TicketResolutionModel.getAll.map { reso => (reso.id.get.toString -> Messages(reso.name)) }

        val assignees = UserModel.getAssignable(projectId = Some(ticket.project.id)).map { user => (user.id.getOrElse("").toString -> Messages(user.realName)) }

        // Find changes
        val changeRes = {
          val changeFilters = Map("ticket_id" -> Seq(ticketId))

          // XXX Different page & count (100?)
          val csm = SearchModel.searchChange(
            page, 100, "", changeFilters
          )
          Page(csm.hits.hits, page, count, csm.hits.totalHits)
        }

        // Find commits
        val commitRes = {
          val commFilters = Map(
            "ticket_id" -> Seq(ticketId),
            "type"      -> Seq("commit")
          )

          val q = SearchQuery(
            userId = request.user.id.get, page = 1,
            count = 99999, query = query, filters = commFilters,
            sortBy = Some("date_created"), sortOrder = Some("asc")
          )
          SearchModel.searchComment(q)
        }

        val commRes = {
          val commFilters = Map(
            "ticket_id" -> Seq(ticketId),
            "type"      -> Seq("comment")
          )

          val q = SearchQuery(
            userId = request.user.id.get, page = 1,
            count = 99999, query = query, filters = commFilters,
            sortBy = Some("date_created"), sortOrder = Some("asc")
          )
          SearchModel.searchComment(q)
        }

        val ret = tab match {
          case "history"  => {

            // val historyFacets = changeRes.facets.facets.map { facet =>
            //   facet match {
            //     case t: InternalStringTermsFacet => t
            //   }
            // } // filter { f => f.entries.size > 1 } // only show facets with > 1 item

            Ok(views.html.ticket.history(
              ticket = ticket,
              assignees = assignees,
              resolutions = resolutions,
              resolveForm = resolveForm,
              assignForm = assignForm.fill(Assignment(ticket.assignee.id, None)),
              commentForm = commentForm,
              comments = commRes,
              commits = commitRes,
              history = changeRes,
              statuses = wfs
            )(request))
          }
          case "commits" => {

             Ok(views.html.ticket.commits(
              ticket = ticket,
              assignees = assignees,
              resolutions = resolutions,
              resolveForm = resolveForm,
              assignForm = assignForm.fill(Assignment(ticket.assignee.id, None)),
              commentForm = commentForm,
              comments = commRes,
              commits = commitRes,
              history = changeRes,
              statuses = wfs
            )(request))
          }
          case _ => {

            Ok(views.html.ticket.comments(
              ticket = ticket,
              assignees = assignees,
              resolutions = resolutions,
              resolveForm = resolveForm,
              assignForm = assignForm.fill(Assignment(ticket.assignee.id, None)),
              commentForm = commentForm,
              comments = commRes,
              commits = commitRes,
              history = changeRes,
              statuses = wfs
            )(request))
          }
        }
        // Populate an in-session list of recently viewed tickets
        val recents: String = session.get("recent_tickets").map({ rt =>
          val rs = rt.split(",").toSeq
          if(rs.contains(ticketId)) {
            // If it's already in there, don't add it again
            rt
          } else {
            // It's not there so append it after reducing the Seq to 9 + 1 items,
            // as we don't want it to grow indefinitely.
            rs.take(9) ++ Seq(ticketId) mkString(",")
          }
        }).getOrElse(ticketId)
        ret.withSession(session + ("recent_tickets" -> recents))
      }
      case None => NotFound
    }
  }

  def link(ticketId: String) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_EDIT") { implicit request =>

    TicketModel.getFullById(ticketId).map({ ticket =>
      val linkTypes = TicketLinkTypeModel.getAll.flatMap(ltype => {
        if(ltype.invertable) {
          // Include the INVERT version with a negative ID so we can pick it up on submission. SO CLEVER.
          List((ltype.id.get.toString -> Messages(ltype.name)), ("-" + ltype.id.get.toString -> Messages(ltype.name + "_INVERT")))
        } else {
          List((ltype.id.get.toString -> Messages(ltype.name)))
        }
      })

      val recents: Seq[FullTicket] = session.get("recent_tickets").map({ rt =>
        rt.split(",").toSeq.map({ rtid =>
          TicketModel.getFullById(rtid).get
        }).reverse
      }).getOrElse(Seq());
      Ok(views.html.ticket.link(ticket, linkTypes, linkForm, recents))
    }).getOrElse(NotFound)
  }

  def doLink(ticketId: String) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_EDIT") { implicit request =>

    TicketModel.getById(ticketId).map({ ticket =>
      linkForm.bindFromRequest.fold(
        errors => {
          Redirect(routes.Ticket.item("comments", ticketId)).flashing("error" -> "ticket.linker.failure")
        }, {
          case value: models.MakeLink => {
            value.comment.map({ comm =>
              TicketModel.addComment(ticketId, "comment", request.user.id.get, comm)
            })

            value.tickets.foreach({ otherTicketId =>
              // Set the parent & child using type.  Negative means to invert.
              // SO CLEVER.
              val ltype = value.linkTypeId
              val parentId = if(ltype < 0) {
                otherTicketId
              } else {
                ticketId
              }
              val childId = if(ltype < 0) {
                ticketId
              } else {
                otherTicketId
              }

              TicketModel.link(
                linkTypeId = abs(ltype), parentId = parentId, childId = childId
              )
            });

            Redirect(routes.Ticket.item("comments", ticketId)).flashing("success" -> "ticket.linker.success")
          }
        }
      )
    }).getOrElse(NotFound)
  }

  def status(ticketId: String) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_EDIT") { implicit request =>

    val ticket = TicketModel.getFullById(ticketId)

    ticket match {
      case Some(value) => {
        val wf = WorkflowModel.getForTicket(ticketId).get;

        statusChangeForm.bindFromRequest.fold(
          errors => {
            Redirect(routes.Ticket.item("comments", ticketId)).flashing("error" -> "ticket.error.status")
          }, {
            case statusChange: models.StatusChange => {
              if(WorkflowModel.verifyStatusInWorkflow(wf.id.get, statusChange.statusId)) {
                TicketModel.changeStatus(ticketId, statusChange.statusId, request.user.id.get, comment = statusChange.comment)
                Redirect(routes.Ticket.item("comments", ticketId)).flashing("success" -> "ticket.success.status")
              } else {
                Redirect(routes.Ticket.item("comments", ticketId)).flashing("error" -> "ticket.error.status.invalid")
              }
            }
          }
        )
      }
      case None => NotFound
    }
  }

  def update(ticketId: String) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_EDIT") { implicit request =>

    TicketModel.getById(ticketId).map({ ticket =>

      ticketForm.bindFromRequest.fold(
        errors => {
          val projs = ProjectModel.getAll(userId = request.user.id.get).map { x => (x.id.get.toString -> Messages(x.name)) }
          val ttypes = TicketTypeModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
          val prios = TicketPriorityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
          val sevs = TicketSeverityModel.getAll.map { x => (x.id.get.toString -> Messages(x.name)) }
          val assignees = UserModel.getAssignable(projectId = Some(ticket.projectId)).map { user => (user.id.getOrElse("").toString -> Messages(user.realName)) }

          BadRequest(views.html.ticket.edit(ticketId, errors, assignees, assignees, assignees, projs, ttypes, prios, sevs))
        },
        value => {
          TicketModel.update(request.user.id.get, ticketId, value)
          Redirect(routes.Ticket.item("comments", ticketId)).flashing("success" -> "ticket.edit.success")
        }
      )
    }).getOrElse(NotFound)
  }
}
