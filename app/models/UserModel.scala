package models

import play.api.db.DB
import play.api.Play.current
import anorm._
import anorm.SqlParser._

case class User(id: Long, username: String, password: String, realName: String, email: String)

object UserModel {

  val allUsersQuery = SQL("SELECT * FROM users")
  val user = {
    get[Long]("id") ~
    get[String]("username") ~
    get[String]("password") ~
    get[String]("realName") ~
    get[String]("email") map {
      case id~username~password~realName~email => User(id, username, password, realName, email)
    }
  }

  def getAllUsers : List[User] = {
      
    DB.withConnection { implicit conn =>
      allUsersQuery.as(user *)
    }
  }
  
  def addUser(username: String, password: String, realName: String, email: String) {
      
  }
  
  def deleteUser(id: Long) {
      
  }
}