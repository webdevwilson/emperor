package emp.event

import akka.event.ActorEventBus
import akka.event.LookupClassification
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import models._

/**
 * Base class for events.
 */
class EmperorEvent(n: String) {
  val name: String = n
}

// XXX Tests for the emission of these events would be awesome.

/**
 * `project/changed`: Represents a modification of a project.
 */
case class ChangeProjectEvent(
  projectId: Long
) extends EmperorEvent("project/changed")

/**
 * `ticket/changed`: Represents a modification of a ticket.
 */
case class ChangeTicketEvent(
  ticketId: String,
  // Will be set to true if this change resolved the ticket
  resolved: Boolean = false,
  newTicketId: Long,
  oldTicketId: Long,
  // Will be set to true if this change unresolved the ticket
  unresolved: Boolean = false
) extends EmperorEvent("ticket/changed")

/**
 * `ticket/commentedon`: Represents a comment added to a ticket.
 */
case class CommentTicketEvent(
  ticketId: String,
  commentId: Long
) extends EmperorEvent("ticket/commentedon")

/**
 * `ticket/linked`: Represents a link created between two tickets.
 */
case class LinkTicketEvent(
  parentId: String,
  childId: String
) extends EmperorEvent("ticket/linked")

/**
 * `user/loggedin`: Represents a user logging in.
 */
case class LogInUserEvent(
  userId: Long
) extends EmperorEvent("user/loggedin")

/**
 * `project/created`: Represents the creation of a project.
 */
case class NewProjectEvent(
  projectId: Long
) extends EmperorEvent("project/created")

/**
 * `ticket/created`: Represents the creation of a ticket.
 */
case class NewTicketEvent(
  ticketId: String
) extends EmperorEvent("ticket/created")

/**
 * `user/created`: Represents the creation of a user.
 */
case class NewUserEvent(
  userId: Long
) extends EmperorEvent("user/created")

/**
 * `ticket/unlinked`: Represents the removal of a link between two tickets.
 */
case class UnlinkTicketEvent(
  childId: String,
  parentId: String
) extends EmperorEvent("ticket/unlinked")

/**
 * Internal event bus used to notify interested plugins about activity within
 * emperor.
 *
 * ==Events==
 *
 * $ - project/changed
 * $ - project/created
 * $ - ticket/changed
 * $ - ticket/commentedon
 * $ - ticket/created
 * $ - ticket/linked
 * $ - ticket/unlinked
 * $ - user/created
 * $ - user/loggedin
 */
object EmperorEventBus extends ActorEventBus with LookupClassification{
  type Event=EmperorEvent
  type Classifier=String

  protected def mapSize(): Int = 10

  protected def classify(event: Event): Classifier = event.name

  protected def publish(event: Event, subscriber: Subscriber): Unit = subscriber ! event
}
