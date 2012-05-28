package models

import anorm._
import anorm.SqlParser._
import chc._
import java.util.Date
import org.mindrot.jbcrypt.BCrypt
import play.api.db.DB
import play.api.Play.current

case class User(id: Pk[Long] = NotAssigned, username: String, password: String, realName: String, email: String, dateCreated: Date)

case class EditUser(username: String, realName: String, email: String)

case class InitialUser(username: String, password: String, realName: String, email: String, dateCreated: Date)

case class LoginUser(username: String, password: String)

case class NewPassword(password: String, password2: String)

object UserModel {

  val allQuery = SQL("SELECT * FROM users")
  val getByIdQuery = SQL("SELECT * FROM users WHERE id={id}")
  val getByGroupIdQuery = SQL("SELECT * FROM users")
  val getByUsernameQuery = SQL("SELECT * FROM users WHERE username={username}")
  val listQuery = SQL("SELECT * FROM users LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM users")
  val insertQuery = SQL("INSERT INTO users (username, password, realname, email, date_created) VALUES ({username}, {password}, {realname}, {email}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE users SET username={username}, realname={realname}, email={email} WHERE id={id}")
  val updatePassQuery = SQL("UPDATE users SET password={password} WHERE id={id}")
  val lastInsertQuery = SQL("SELECT LAST_INSERT_ID()")

  val user = {
    get[Pk[Long]]("id") ~
    get[String]("username") ~
    get[String]("password") ~
    get[String]("realName") ~
    get[String]("email") ~
    get[Date]("date_created") map {
      case id~username~password~realName~email~dateCreated => User(id, username, password, realName, email, dateCreated)
    }
  }

  def create(user: InitialUser): User = {

    DB.withConnection { implicit conn =>
      insertQuery.on(
        'username   -> user.username,
        'password   -> BCrypt.hashpw(user.password, BCrypt.gensalt(12)),
        'realname   -> user.realName,
        'email      -> user.email
      ).executeUpdate

      val id = lastInsertQuery.as(scalar[Long].single)
      this.getById(id).get
    }
  }
  
  def delete(id: Long) {
      
  }

  def getById(id: Long) : Option[User] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(UserModel.user.singleOpt)
    }
  }

  def getAll: List[User] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(user *)
    }
  }

  def getByUsername(username: String) : Option[User] = {
      
    DB.withConnection { implicit conn =>
      getByUsernameQuery.on('username -> username).as(UserModel.user.singleOpt)
    }
  }

  def list(page: Int = 0, count: Int = 10) : Page[User] = {

      val offset = count * page
      
      DB.withConnection { implicit conn =>
        val users = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(user *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(users, page, count, totalRows)
      }
  }
  
  def update(id: Long, user: EditUser) = {

    DB.withTransaction { implicit conn =>
      val foo = updateQuery.on(
        'id         -> id,
        'username   -> user.username,
        'realname   -> user.realName,
        'email      -> user.email
      ).executeUpdate
    }
  }

  def updatePassword(id: Long, np: NewPassword) = {

    val hashedPass = BCrypt.hashpw(np.password, BCrypt.gensalt(12))

    DB.withTransaction { implicit conn =>
      val foo = updatePassQuery.on(
        'id         -> id,
        'password   -> hashedPass
      ).executeUpdate
    }
  }
}