package emp

import akka.event.ActorEventBus
import akka.event.LookupClassification
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import models._

class EmperorEvent(n: String) {
  val name: String = n
}

case class ChangedTicketEvent(
  ticketId: String
) extends EmperorEvent("ticket/changed")

case class LinkTicketEvent(
  ticketId: String
) extends EmperorEvent("ticket/linked")

case class NewTicketEvent(
  ticketId: String
) extends EmperorEvent("ticket/created")

case class UnlinkTicketEvent(
  ticketId: String
) extends EmperorEvent("ticket/unlinked")

// Found help from https://gist.github.com/3163791

object EmperorEventBus extends ActorEventBus with LookupClassification{
  type Event=EmperorEvent
  type Classifier=String

  protected def mapSize(): Int = 10

  protected def classify(event: Event): Classifier = event.name

  protected def publish(event: Event, subscriber: Subscriber): Unit = subscriber ! event
}
