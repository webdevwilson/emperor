package models

import play.api.db.DB
import play.api.Play.current
import anorm._
import anorm.SqlParser._

case class User(id: Pk[Long], username: String, password: String, realName: String, email: String)

object UserModel {

  val allUsersQuery = SQL("SELECT * FROM users")
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

  def getAllUsers: List[User] = {
      
    DB.withConnection { implicit conn =>
      allUsersQuery.as(user *)
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
}