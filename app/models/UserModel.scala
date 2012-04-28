package models

import anorm._
import anorm.SqlParser._
import chc._
import play.api.db.DB
import play.api.Play.current

case class User(id: Pk[Long] = NotAssigned, username: String, password: String, realName: String, email: String)

object UserModel {

  val allUsersQuery = SQL("SELECT * FROM users")
  val getUserByIdQuery = SQL("SELECT * FROM users WHERE id={id}")
  val listUsersQuery = SQL("SELECT * FROM users LIMIT {offset},{count}")
  val listUsersCountQuery = SQL("SELECT count(*) FROM users")
  val insertUserQuery = SQL("INSERT INTO users (username, password, realname, email) VALUES ({username}, {password}, {realname}, {email})")

  val user = {
    get[Pk[Long]]("id") ~
    get[String]("username") ~
    get[String]("password") ~
    get[String]("realName") ~
    get[String]("email") map {
      case id~username~password~realName~email => User(id, username, password, realName, email)
    }
  }

  def createUser(user: User): User = {

    DB.withConnection { implicit conn =>
      insertUserQuery.on(
        "username"  -> user.username,
        "password"  -> user.password,
        "realname"  -> user.realName,
        "email"     -> user.email
      ).executeUpdate
    }
    
    user
  }
  
  def deleteUser(id: Long) {
      
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
}