import play.api._
import play.db.DB
import models.SearchModel

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
    SearchModel.checkIndices
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
    SearchModel.shutdown
  }
}