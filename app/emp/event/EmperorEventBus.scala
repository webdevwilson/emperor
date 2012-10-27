package emp.event

import akka.event.ActorEventBus
import akka.event.LookupClassification
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import models._

class EmperorEvent(n: String) {
  val name: String = n
}

// XXX Tests for the emission of these events would be awesome.

case class ChangeProjectEvent(
  projectId: Long
) extends EmperorEvent("project/changed")

case class ChangeTicketEvent(
  ticketId: String,
  // Will be set to true if this change resolved the ticket
  resolved: Boolean = false,
  // Will be set to true if this change unresolved the ticket
  unresolved: Boolean = false
) extends EmperorEvent("ticket/changed")

case class CommentTicketEvent(
  ticketId: String
) extends EmperorEvent("ticket/commentedon")

case class LinkTicketEvent(
  parentId: String,
  childId: String
) extends EmperorEvent("ticket/linked")

case class LogInUserEvent(
  userId: Long
) extends EmperorEvent("user/loggedin")

case class NewProjectEvent(
  projectId: Long
) extends EmperorEvent("project/created")

case class NewTicketEvent(
  ticketId: String
) extends EmperorEvent("ticket/created")

case class NewUserEvent(
  userId: Long
) extends EmperorEvent("user/created")

case class UnlinkTicketEvent(
  childId: String,
  parentId: String
) extends EmperorEvent("ticket/unlinked")

// Found help from https://gist.github.com/3163791

object EmperorEventBus extends ActorEventBus with LookupClassification{
  type Event=EmperorEvent
  type Classifier=String

  protected def mapSize(): Int = 10

  protected def classify(event: Event): Classifier = event.name

  protected def publish(event: Event, subscriber: Subscriber): Unit = subscriber ! event
}
