package emp.plugin

import akka.actor.Actor
import emp._

class EmailNotifier extends Actor {

  def receive = {
    case d: EmperorEvent => println(d)
  }
}

// XXX This should be a trait?
object EmailNotifier {

  def relevantEvents = List("ticket/changed", "ticket/created")
}