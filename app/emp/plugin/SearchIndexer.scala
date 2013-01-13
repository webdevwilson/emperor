package emp.plugin

import akka.actor.Actor
import emp.Plugin
import emp.event._
import models.{SearchModel,TicketModel}
import play.api.Configuration
import play.api.Logger

class SearchIndexer(configuration: Configuration) extends Actor {

  /**
   * Receive an event.
   */
  def receive = {
    case event: EmperorEvent => doIndex(event)
  }

  /**
   * Index something based on the event received.
   */
  def doIndex(event: EmperorEvent) = {

    event match {
      // Handle a change to a ticket
      case cte: ChangeTicketEvent => {
        TicketModel.getFullById(cte.ticketId).map({ ft =>
          Logger.debug("Received change event for " + ft.ticketId + ", indexing.")
          SearchModel.indexTicket(ticket = ft)

          val newTicket = TicketModel.getFullByActualId(cte.newTicketId)
          val oldTicket = TicketModel.getFullByActualId(cte.oldTicketId)

          SearchModel.indexHistory(newTick = newTicket.get, oldTick = oldTicket.get, block = true)
        })
      }
      // Handle a ticket comment
      case tcomm: CommentTicketEvent => {
        TicketModel.getCommentById(tcomm.commentId).map({ c =>
          SearchModel.indexComment(c, true)
        })
      }
      // Handle a new ticket
      case nte: NewTicketEvent => {
        TicketModel.getFullById(nte.ticketId).map({ ft =>
          Logger.debug("Received create event for " + ft.ticketId + ", indexing.")
          SearchModel.indexTicket(ticket = ft)

          SearchModel.indexEvent(models.Event(
            projectId     = ft.project.id,
            projectName   = ft.project.name,
            userId        = ft.user.id,
            userRealName  = ft.user.name,
            eKey          = ft.ticketId,
            eType         = "ticket_create",
            content       = ft.summary,
            url           = controllers.routes.Ticket.item("comments", ft.ticketId).url,
            dateCreated   = ft.dateCreated
          ), true)
        })
      }
      // Unknown event, just do nothing
      case _ => None
    }
  }
}

object SearchIndexer extends Plugin {

  def relevantEvents = List("ticket/changed", "ticket/commentedon", "ticket/created")
}