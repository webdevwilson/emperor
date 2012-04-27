import play.api._
import play.db.DB
import org.squeryl.{Session,SessionFactory}
import org.squeryl.adapters.MySQLAdapter

object Global extends GlobalSettings {

  val dbAdapter = new MySQLAdapter(); 

  override def onStart(app: Application) {
    Logger.info("Application has started")

    SessionFactory.concreteFactory = Some( 
            () => Session.create(DB.getDataSource().getConnection(), 
                                dbAdapter)
    )
  }  
  
  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }     
}