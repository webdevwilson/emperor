package controllers.api

import anorm.{NotAssigned,Pk}
import emp.JsonFormats._
import controllers._
import models._
import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.libs.json.Json
import play.api.libs.Jsonp
import play.api.mvc._

object Ticket extends Controller with Secured {

  val ticketForm = Form(
    mapping(
      "projectId"  -> ignored(1L),
      "typeId"     -> longNumber,
      "priorityId" -> longNumber,
      "severityId" -> longNumber,
      "summary"     -> nonEmptyText,
      "description" -> optional(text),
      "assigneeId" -> optional(longNumber),
      "position"    -> optional(longNumber)
    )(models.NewTicket.apply)(models.NewTicket.unapply)
  )

  def create(projectId: Long, callback: Option[String]) = IsAuthenticated(projectId = Some(projectId), perm = "PERM_TICKET_CREATE") { implicit request =>

    request.body.asJson.map({ data =>
      ticketForm.bind(data).fold(
        errors => {
          BadRequest(errors.errorsAsJson)
        },
        value => {
          TicketModel.create(
            userId = request.user.id.get, projectId = projectId, typeId = value.typeId, priorityId = value.priorityId,
            severityId = value.severityId, summary = value.summary, description = value.description,
            assigneeId = value.assigneeId, position = value.position
          ).fold(
            error => {
              BadRequest(Json.toJson(Map("error" -> Messages(error))))
            },
            ticket => {
              val json = Json.toJson(ticket)
              // Inference goes nuts here unless we type this result
              val res: Result = callback.map({ cb => Ok(Jsonp(cb, json)) }).getOrElse(Ok(json))
              res
            }
          )
        }
      )
    }).getOrElse({
      BadRequest("Expecting JSON data.")
    })
  }

  def item(ticketId: String, callback: Option[String]) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_PROJECT_BROWSE") { implicit request =>

    val ticket = TicketModel.getFullByStringId(ticketId)

    ticket match {
      case Some(t) => {

        val json = Json.toJson(t)
        callback match {
          case Some(callback) => Ok(Jsonp(callback, json))
          case None => Ok(json)
        }
      }
      case None => NotFound
    }
  }

  def deleteLink(ticketId: String, id: Long, callback: Option[String]) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_LINK") { implicit request =>

    val (tProj, tId) = TicketModel.parseTicketId(ticketId).get

    val maybeLink = TicketModel.getLinkById(id).map({ link =>
      if(link.parentId == tId || link.childId == tId) {
        link
      } else {
        None
      }
    })

    maybeLink match {
      case Some(link) => {
        TicketModel.removeLink(id)
        val json = Json.toJson(Map("ok" -> "ok"))
        callback match {
          case Some(callback) => Ok(Jsonp(callback, json))
          case None => Ok(json)
        }
      }
      case None => NotFound
    }
  }

  def link(ticketId: String, callback: Option[String]) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_TICKET_LINK") { implicit request =>

    request.body.asJson.map { json =>
      val childId = (json \ "child_ticket_id").asOpt[String]
      val typeId = (json \ "link_type_id").asOpt[Long]

      val maybeLink: Either[String,Option[FullLink]] = if(childId.isDefined && typeId.isDefined) {
        if(childId.get == ticketId) {
          Left("Can't link ticket to itself.")
        } else {
          val parent = TicketModel.getByStringId(ticketId)
          val child = TicketModel.getByStringId(childId.get)

          if(parent.isDefined && child.isDefined) {
            Right(TicketModel.link(
              linkTypeId = typeId.get, parentId = parent.get.id.get, childId = child.get.id.get
            ))
          } else {
            Left("Unknown ticket id.")
          }
        }
      } else {
        Left("Must have both child_ticket_id and link_type_id")
      }

      maybeLink match {
        case Left(message) => BadRequest(message)
        case Right(link) => link match {
          case Some(l) => {
            val json = Json.toJson(l)
            callback match {
              case Some(callback) => Ok(Jsonp(callback, json))
              case None => Ok(json)
            }
          }
          case None => InternalServerError("Error occurred creating link.")
        }
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def links(ticketId: String, callback: Option[String]) = IsAuthenticated(ticketId = Some(ticketId), perm = "PERM_PROJECT_BROWSE") { implicit request =>

    val json = TicketModel.getByStringId(ticketId).map({ ticket =>

      // XXX Need to put the actual tickets in here, at least the Edit ticket
      Json.toJson(TicketModel.getLinks(ticket.id.get))
    }).getOrElse(Json.obj("error" -> "Ticket not found"))

    callback match {
      case Some(callback) => Ok(Jsonp(callback, json))
      case None => Ok(json)
    }
  }

  def search(page: Int, count: Int, query: String, sort: Option[String] = None, order: Option[String] = None, callback: Option[String]) = IsAuthenticated() { implicit request =>

    val filters = request.queryString filterKeys { key =>
      SearchModel.ticketFilterMap.get(key).isDefined
    }

    val userId = request.user.id.get
    val sort = request.queryString.get("sort").map({ vals => Some(vals.head) }).getOrElse(None);
    val order = request.queryString.get("order").map({ vals => Some(vals.head) }).getOrElse(None);
    val q = emp.util.Search.SearchQuery(
      userId = userId, page = page, count = count, query = query,
      filters = filters, sortBy = sort, sortOrder = order, parser = "match"
    )
    val res = SearchModel.searchTicket(q)

    val tickets = res.pager.items.map({ ticket => ticket }).toSeq;

    callback match {
      case Some(callback) => Ok(Jsonp(callback, Json.toJson(tickets)))
      case None => Ok(Json.toJson(tickets))
    }
  }

  def startsWith(q: Option[String], callback: Option[String]) = IsAuthenticated() { implicit request =>

    q match {
      case Some(query) => {

        val sq = emp.util.Search.SearchQuery(
          userId = request.user.id.get, page = 1, count = 10, query = "ticket_id: " + query + "*"
        )
        val res = SearchModel.searchTicket(sq)

        val tickets = res.pager.items.map({ ticket => ticket }).toSeq;

        callback match {
          case Some(callback) => Ok(Jsonp(callback, Json.toJson(tickets)))
          case None => Ok(Json.toJson(tickets))
        }
      }
      case None => NotFound
    }
  }
}