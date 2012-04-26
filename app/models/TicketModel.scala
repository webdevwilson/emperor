package models

import play.api._
import play.db._
import chc._
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.ql._

object TicketModel {
  
  val db = Database.forDataSource(DB.getDataSource)

  val allResolutionsQuery = for(p <- TicketResolutions) yield p.id ~ p.name
  val allTypesQuery = for(p <- TicketTypes) yield p.*

  def getAllResolutions : List[(Int,String)] = {

    db withSession {
      allResolutionsQuery.list
    }
  }

  def getAllTypes : List[TicketType] = {
      
    db withSession {
      allTypesQuery.list
    }
  }
}