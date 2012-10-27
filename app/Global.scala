import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import emp.event._
import emp.plugin._
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

    val emailNotifier = system.actorOf(Props(new EmailNotifier(Play.configuration)))

    EmailNotifier.relevantEvents.foreach { ev =>
      Logger.debug("Subscribed Email Notifier to '" + ev + "'")
      EmperorEventBus.subscribe(emailNotifier, ev)
    }
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
    if(!Play.isTest) {
      SearchModel.shutdown
    }
  }
}