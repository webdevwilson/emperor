package models

import anorm._
import anorm.SqlParser._
import chc._
import org.mindrot.jbcrypt.BCrypt
import play.api.db.DB
import play.api.Play.current

case class User(id: Pk[Long] = NotAssigned, username: String, password: String, realName: String, email: String)

object UserModel {

  val allQuery = SQL("SELECT * FROM users")
  val getByIdQuery = SQL("SELECT * FROM users WHERE id={id}")
  val listQuery = SQL("SELECT * FROM users LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM users")
  val insertQuery = SQL("INSERT INTO users (username, password, realname, email) VALUES ({username}, {password}, {realname}, {email})")
  val updateQuery = SQL("UPDATE users SET username={username}, password={password}, realname={realname}, email={email} WHERE id={id}")

  val user = {
    get[Pk[Long]]("id") ~
    get[String]("username") ~
    get[String]("password") ~
    get[String]("realName") ~
    get[String]("email") map {
      case id~username~password~realName~email => User(id, username, password, realName, email)
    }
  }

  def create(user: User): User = {

    DB.withConnection { implicit conn =>
      insertQuery.on(
        'username   -> user.username,
        'password   -> BCrypt.hashpw(user.password, BCrypt.gensalt(12)),
        'realname   -> user.realName,
        'email      -> user.email
      ).executeUpdate
    }
    
    user
  }
  
  def delete(id: Long) {
      
  }

  def findById(id: Long) : Option[User] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(UserModel.user.singleOpt)
    }
  }

  def getAll: List[User] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(user *)
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
  
  def update(id: Long, user: User) = {

    val hashedPass = BCrypt.hashpw(user.password, BCrypt.gensalt(12))

    DB.withTransaction { implicit conn =>
      val foo = updateQuery.on(
        'id         -> id,
        'username   -> user.username,
        'password   -> hashedPass,
        'realname   -> user.realName,
        'email      -> user.email
      ).executeUpdate
    }
  }
}