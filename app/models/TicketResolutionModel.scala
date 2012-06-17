package models

import anorm._
import anorm.SqlParser._
import chc._
import java.util.Date
import play.api.db.DB
import play.api.Play.current

case class TicketResolution(id: Pk[Long] = NotAssigned, name: String, dateCreated: Date)

object TicketResolutionModel {

  val allQuery = SQL("SELECT * FROM ticket_resolutions ORDER BY id")
  val getByIdQuery = SQL("SELECT * FROM ticket_resolutions WHERE id={id}")
  val listQuery = SQL("SELECT * FROM ticket_resolutions LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM ticket_resolutions")
  val insertQuery = SQL("INSERT INTO ticket_resolutions (name, date_created) VALUES ({name}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE ticket_resolutions SET name={name} WHERE id={id}")

  val ticket_resolution = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[Date]("date_created") map {
      case id~name~dateCreated => TicketResolution(id, name, dateCreated)
    }
  }

  def create(tr: TicketResolution): TicketResolution = {

    DB.withConnection { implicit conn =>
      val id = insertQuery.on(
        'name   -> tr.name
      ).executeInsert()

      this.getById(id.get).get
    }
  }
  
  def delete(id: Long) {
      
  }

  def getById(id: Long) : Option[TicketResolution] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(ticket_resolution.singleOpt)
    }
  }

  def getAll: List[TicketResolution] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(ticket_resolution *)
    }
  }

  def list(page: Int = 1, count: Int = 10) : Page[TicketResolution] = {

      val offset = count * (page - 1)
      
      DB.withConnection { implicit conn =>
        val trs = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(ticket_resolution *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(trs, page, offset, totalRows)
      }
  }
  
  def update(id: Long, tr: TicketResolution) = {

    DB.withTransaction { implicit conn =>
      val foo = updateQuery.on(
        'id         -> id,
        'name       -> tr.name
      ).executeUpdate
    }
  }
}