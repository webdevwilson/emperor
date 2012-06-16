package models

import anorm._
import anorm.SqlParser._
import chc._
import java.util.Date
import play.api.db.DB
import play.api.Play.current

case class TicketLinkType(id: Pk[Long] = NotAssigned, name: String, dateCreated: Date)

object TicketLinkTypeModel {

  val allQuery = SQL("SELECT * FROM ticket_link_types")
  val getByIdQuery = SQL("SELECT * FROM ticket_link_types WHERE id={id}")
  val listQuery = SQL("SELECT * FROM ticket_link_types LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM ticket_link_types")
  val insertQuery = SQL("INSERT INTO ticket_link_types (name, date_created) VALUES ({name}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE ticket_link_types SET name={name} WHERE id={id}")

  val ticket_link_type = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[Date]("date_created") map {
      case id~name~dateCreated => TicketLinkType(id, name, dateCreated)
    }
  }

  def create(ts: TicketLinkType): TicketLinkType = {

    DB.withConnection { implicit conn =>
      insertQuery.on(
        'name   -> ts.name
      ).executeUpdate
    }
    
    ts
  }
  
  def delete(id: Long) {
      
  }

  def getById(id: Long) : Option[TicketLinkType] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(ticket_link_type.singleOpt)
    }
  }

  def getAll: List[TicketLinkType] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(ticket_link_type *)
    }
  }

  def list(page: Int = 1, count: Int = 10) : Page[TicketLinkType] = {

      val offset = count * (page - 1)
      
      DB.withConnection { implicit conn =>
        val tss = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(ticket_link_type *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(tss, page, count, totalRows)
      }
  }
  
  def update(id: Long, ts: TicketLinkType) = {

    DB.withTransaction { implicit conn =>
      val foo = updateQuery.on(
        'id         -> id,
        'name       -> ts.name
      ).executeUpdate
    }
  }
}