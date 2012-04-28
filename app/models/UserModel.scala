package models

import anorm._
import anorm.SqlParser._
import chc._
import org.mindrot.jbcrypt.BCrypt
import play.api.db.DB
import play.api.Play.current

case class User(id: Pk[Long] = NotAssigned, username: String, password: String, realName: String, email: String)

object UserModel {

  val allUsersQuery = SQL("SELECT * FROM users")
  val getUserByIdQuery = SQL("SELECT * FROM users WHERE id={id}")
  val listUsersQuery = SQL("SELECT * FROM users LIMIT {offset},{count}")
  val listUsersCountQuery = SQL("SELECT count(*) FROM users")
  val insertUserQuery = SQL("INSERT INTO users (username, password, realname, email) VALUES ({username}, {password}, {realname}, {email})")
  val updateUserQuery = SQL("UPDATE users SET username={username}, password={password}, realname={realname}, email={email} WHERE id={id}")

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
      insertUserQuery.on(
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
      getUserByIdQuery.on('id -> id).as(UserModel.user.singleOpt)
    }
  }

  def getAll: List[User] = {
      
    DB.withConnection { implicit conn =>
      allUsersQuery.as(user *)
    }
  }

  def list(page: Int = 0, count: Int = 10) : Page[User] = {

      val offset = count * page
      
      DB.withConnection { implicit conn =>
        val users = listUsersQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(user *)

        val totalRows = listUsersCountQuery.as(scalar[Long].single)

        Page(users, page, offset, totalRows)
      }
  }
  
  def update(id: Long, user: User) = {

    val hashedPass = BCrypt.hashpw(user.password, BCrypt.gensalt(12))
    println("HASHED " + hashedPass)

    DB.withTransaction { implicit conn =>
      val foo = updateUserQuery.on(
        'id         -> id,
        'username   -> user.username,
        'password   -> hashedPass,
        'realname   -> user.realName,
        'email      -> user.email
      ).executeUpdate
      println("Affected: " + foo)
    }
  }
}