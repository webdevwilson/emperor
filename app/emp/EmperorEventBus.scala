package emp

import akka.event.ActorEventBus
import akka.event.LookupClassification
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import models._

case class EmperorEvent(
  name: String
)

object EmperorEventBus extends ActorEventBus with LookupClassification{
  type Event=EmperorEvent
  type Classifier=String

  protected def mapSize(): Int = 10

  protected def classify(event: Event): Classifier = event.name

  protected def publish(event: Event, subscriber: Subscriber): Unit = subscriber ! event
}
