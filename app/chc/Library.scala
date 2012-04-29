package chc

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
  val firstPage = 1
  lazy val lastPage = 1 // XXX Fix this calculation
}

// import org.scalaquery.session._
// import org.scalaquery.session.Database.threadLocalSession
// import org.scalaquery.ql.basic.BasicDriver.Implicit._
// import org.scalaquery.ql.extended.{ExtendedTable => Table}
// import org.scalaquery.ql.TypeMapper._
// import org.scalaquery.ql._
// import java.sql.Timestamp
// import org.squeryl.Schema
// import org.squeryl.annotations._
// 
// class Project(
//   val id : Long,
//   val name : String,
//   @Column("date_created")
//   val dateCreated : Timestamp) {
// }

// object Projects extends Table[(Int, String, Timestamp)]("projects") {
//   def id = column[Int]("id")
//   def name = column[String]("name")
//   def dateCreated = column[Timestamp]("date_created")
//   def * = id ~ name ~ dateCreated
// }

// class User(
//   val id : Long,
//   val username : String,
//   val password : String,
//   @Column("real_name")
//   val realName : String,
//   val email : String,
//   @Column("date_created")
//   val dateCreated : Timestamp) {
// }
 
// case class User(id: Int, username: String, password: String, realName: String, email: String, dateCreated: Timestamp)
// 
// object Users extends Table[User]("users") {
//   def id = column[Int]("id", O PrimaryKey, O AutoInc)
//   def username = column[String]("username", O NotNull)
//   def password = column[String]("password")
//   def realName = column[String]("realname")
//   def email = column[String]("email")
//   def dateCreated = column[Timestamp]("date_created")
//   def * = id ~ username ~ password ~ realName ~ email ~ dateCreated <> (User, User.unapply _)
// }
// 
// object Groups extends Table[(Int, String, Timestamp)]("groups") {
//   def id = column[Int]("id")
//   def name = column[String]("username")
//   def dateCreated = column[Timestamp]("date_created")
//   def * = id ~ name ~ dateCreated
// }
// 
// object GroupUsers extends Table[(Int, Int, Int, Timestamp)]("group_users") {
//   def id = column[Int]("id")
//   def groupId = column[Int]("group_id")
//   def userId = column[Int]("user_id")
//   def dateCreated = column[Timestamp]("date_created")
//   def * = id ~ groupId ~ userId ~ dateCreated
//   def group = foreignKey("fk_group_users_group_id", groupId, Groups)(_.id)
//   def user = foreignKey("fk_group_users_user_id", userId, Users)(_.id) 
// }
// 
// object TicketResolutions extends Table[(Int, String)]("ticket_resolutions") {
//   def id = column[Int]("id")
//   def name = column[String]("name")
//   def * = id ~ name    
// }
// 
// object TicketStatuses extends Table[(Int, String)]("ticket_statuses") {
//   def id = column[Int]("id")
//   def name = column[String]("name")
//   def * = id ~ name    
// }
// 
// case class TicketType(id: Int, name: String)

// class TicketType(
//   val id : Long,
//   val name : String
// )

// object TicketTypes extends Table[TicketType]("ticket_types") {
//   def id = column[Int]("id")
//   def name = column[String]("name")
//   def * = id ~ name <> (TicketType, TicketType.unapply _)
// }
// 
// object Tickets extends Table[(Int, Int, Int, Int, Option[Int], String, Option[String], Timestamp)]("tickets") {
//   def id = column[Int]("id")
//   def resolutionId = column[Int]("ticket_resolution_id")
//   def statusId = column[Int]("ticket_status_id")
//   def typeId = column[Int]("ticket_type_id")
//   def position = column[Option[Int]]("position")
//   def summary = column[String]("title")
//   def description = column[Option[String]]("description")
//   def dateCreated = column[Timestamp]("date_created")
//   def * = id ~ resolutionId ~ statusId ~ typeId ~ position ~ summary ~ description ~ dateCreated
//   def resolution = foreignKey("fk_ticket_resolution_id", resolutionId, TicketResolutions)(_.id)
//   def status = foreignKey("fk_ticket_status_id", resolutionId, TicketStatuses)(_.id)        
//   def ttype = foreignKey("fk_ticket_type_id", resolutionId, TicketTypes)(_.id)
// }
// 
// object ProjectTickets extends Table[(Int, Int, Int)]("project_tickets") {
//   def id = column[Int]("id")
//   def projectId = column[Int]("project_id")
//   def ticketId = column[Int]("ticket_id")
//   def * = id ~ projectId ~ ticketId
//   def project = foreignKey("fk_project_tickets_project_id", projectId, Projects)(_.id)
//   def ticket = foreignKey("fk_project_tickets", ticketId, Tickets)(_.id)
// }
// 
// object TicketLinkTypes extends Table[(Int, String)]("ticket_link_types") {
//     def id = column[Int]("id")
//     def name = column[String]("name")
//     def * = id ~ name
// }
// 
// object TicketLinks extends Table[(Int, Int, Int, Int, Timestamp)]("ticket_links") {
//   def id = column[Int]("id")
//   def linkTypeId = column[Int]("link_type_id")
//   def parentTicketId = column[Int]("parent_ticket_id")
//   def childTicketId = column[Int]("child_ticket_id")
//   def dateCreated = column[Timestamp]("date_created")
//   def * = id ~ linkTypeId ~ parentTicketId ~ childTicketId ~ dateCreated
//   def linkType = foreignKey("fk_ticket_links_link_type_id", linkTypeId, TicketLinkTypes)(_.id)
//   def parentTicket = foreignKey("fk_ticket_links_parent_ticket_id", parentTicketId, Tickets)(_.id)
//   def childTicket = foreignKey("fk_ticket_links_child_ticket_id", childTicketId, Tickets)(_.id)
// }

// object Library extends Schema {
//     val users = table[User]("users")
//     val ticketTypes = table[TicketType]("ticket_types")
// }
