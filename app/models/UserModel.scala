package models

import play.api._
import play.db._
import chc._
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.ql._

object UserModel {
  
  val db = Database.forDataSource(DB.getDataSource)

  val allUsersQuery = for(u <- Users) yield u.*

  def getAllUsers : List[User] = {
      
    db withSession {
      allUsersQuery.list
    }
  }
}