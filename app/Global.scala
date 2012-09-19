import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import chc._
import play.api._
import play.api.Play.current
import play.db.DB
import models.SearchModel

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
    if(!Play.isTest) {
      SearchModel.checkIndices
    }

    val system = ActorSystem("Emperor")

    val subscriber = system.actorOf(Props(new Actor {
      def receive = {
        case d: EmperorEvent => println(d)
      }
    }))

    EmperorEventBus.subscribe(subscriber, "poop")
    EmperorEventBus.publish(EmperorEvent("poop"))
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
    if(!Play.isTest) {
      SearchModel.shutdown
    }
  }
}