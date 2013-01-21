package models

import anorm._
import anorm.SqlParser._
import emp.util.AnormExtension._
import emp.event._
import emp.util.Pagination.Page
import org.joda.time.DateTime
import org.apache.commons.codec.digest.DigestUtils
import org.mindrot.jbcrypt.BCrypt
import play.api.db.DB
import play.api.i18n.Messages
import play.api.Play.current

case class User(
  id: Pk[Long] = NotAssigned,
  username: String,
  password: String,
  realName: String,
  timezone: String,
  email: String,
  organization: Option[String],
  location: Option[String],
  title: Option[String],
  url: Option[String],
  dateCreated: DateTime
) {

  def isAnonymous = username.equals("anonymous")
}

case class LoginUser(username: String, password: String)

case class NewPassword(password: String, password2: String)

object UserModel {

  val allQuery = SQL("SELECT * FROM users")
  val getAllAssignableQuery = SQL("SELECT u.* FROM full_permissions AS fp JOIN users u on u.id = fp.user_id WHERE permission_id IN ('PERM_PROJECT_ADMIN', 'PERM_PROJECT_BROWSE', 'PERM_GLOBAL_ADMIN') AND project_id={project_id} GROUP BY u.id")
  val getByIdQuery = SQL("SELECT * FROM users WHERE id={id}")
  val getByEmailQuery = SQL("SELECT * from users WHERE email={email}")
  val getByGroupIdQuery = SQL("SELECT * FROM users")
  val getByUsernameQuery = SQL("SELECT * FROM users WHERE username={username}")
  val getIdByUsernameQuery = SQL("SELECT id FROM users WHERE username={username}")
  val listQuery = SQL("SELECT * FROM users ORDER BY username LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM users")
  val insertQuery = SQL("INSERT INTO users (username, password, realname, email, timezone, organization, location, title, url, date_created) VALUES ({username}, {password}, {realname}, {email}, {timezone}, {organization}, {location}, {title}, {url}, UTC_TIMESTAMP())")
  val startsWithQuery = SQL("SELECT * FROM users WHERE username COLLATE utf8_unicode_ci LIKE {username}")
  val updateQuery = SQL("UPDATE users SET username={username}, realname={realname}, email={email}, timezone={timezone}, organization={organization}, location={location}, title={title}, url={url} WHERE id={id}")
  val updatePassQuery = SQL("UPDATE users SET password={password} WHERE id={id}")
  val deleteQuery = SQL("DELETE FROM users WHERE id={id}")

  val user = {
    get[Pk[Long]]("id") ~
    get[String]("username") ~
    get[String]("password") ~
    get[String]("realName") ~
    get[String]("timezone") ~
    get[String]("email") ~
    get[Option[String]]("organization") ~
    get[Option[String]]("location") ~
    get[Option[String]]("title") ~
    get[Option[String]]("url") ~
    get[DateTime]("date_created") map {
      case id~username~password~realName~timezone~email~organization~location~title~url~dateCreated => User(id, username, password, realName, timezone, email, organization, location, title, url, dateCreated)
    }
  }

  /**
   * Add a user.
   */
  def create(user: User): User = {

    DB.withConnection { implicit conn =>
      val id = insertQuery.on(
        'username   -> user.username,
        'password   -> BCrypt.hashpw(user.password, BCrypt.gensalt(12)),
        'realname   -> user.realName,
        'organization -> user.organization,
        'location   -> user.location,
        'title      -> user.title,
        'url        -> user.url,
        'email      -> user.email,
        'timezone   -> user.timezone
      ).executeInsert()

      id.map { uid =>
        EmperorEventBus.publish(
          NewUserEvent(
            userId = uid
          )
        )
      }

      this.getById(id.get).get
    }
  }

  /**
   * Delete a user.
   */
  def delete(id: Long) {
    DB.withConnection { implicit conn =>
      deleteQuery.on(
        'id -> id
      ).execute
    }
  }

  /**
   * Retrieve a user by id.
   */
  def getById(id: Long) : Option[User] = {

    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(user.singleOpt)
    }
  }

  /**
   * Retrieve a user by email.
   */
  def getByEmail(email: String) : List[User] = {

    DB.withConnection { implicit conn =>
      getByEmailQuery.on('email -> email).as(user *)
    }
  }

  def getAll: List[User] = {

    DB.withConnection { implicit conn =>
      allQuery.as(user *)
    }
  }

  def getAssignable(projectId: Option[Long], ticketId: Option[String] = None): List[User] = {

    val users = projectId.map({ pid =>
      DB.withConnection { implicit conn =>
        getAllAssignableQuery.on('project_id -> pid).as(user *)
      }
    }).getOrElse(UserModel.getAll)

    // Add the nobody. In the future this will likely be conditional based
    // on a project setting for allowing unassigned tickets or something.
    User(
      id       = new Id(0.toLong),
      username = "",
      password = "",
      realName = Messages("ticket.unassigned"),
      email    = "",
      timezone = "",
      organization = None,
      location = None,
      title    = None,
      url      = None,
      dateCreated = new DateTime()
    ) +: users
  }

  def getByUsername(username: String) : Option[User] = {

    DB.withConnection { implicit conn =>
      getByUsernameQuery.on('username -> username).as(UserModel.user.singleOpt)
    }
  }

  def getIdByUsername(username: String) : Option[Long] = {

    DB.withConnection { implicit conn =>
      getIdByUsernameQuery.on('username -> username).as(scalar[Long].singleOpt)
    }
  }

  /**
   * Find all users starting with a specific string. Used for
   * autocomplete.
   */
  def getStartsWith(query: String) : Seq[User] = {

    val likeQuery = query + "%"

    DB.withConnection { implicit conn =>
      startsWithQuery.on(
        'username -> likeQuery
      ).as(user *)
    }
  }

  def list(page: Int = 1, count: Int = 10) : Page[User] = {

      val offset = count * (page - 1)

      DB.withConnection { implicit conn =>
        val users = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(user *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(users, page, count, totalRows)
      }
  }

  def update(id: Long, user: User): Option[User] = {

    DB.withConnection { implicit conn =>
      updateQuery.on(
        'id         -> id,
        'username   -> user.username,
        'realname   -> user.realName,
        'organization -> user.organization,
        'location   -> user.location,
        'title      -> user.title,
        'url        -> user.url,
        'email      -> user.email,
        'timezone   -> user.timezone
      ).execute
      getById(id)
    }
  }

  def updatePassword(id: Long, np: NewPassword) = {

    val hashedPass = BCrypt.hashpw(np.password, BCrypt.gensalt(12))

    DB.withConnection { implicit conn =>
      val foo = updatePassQuery.on(
        'id         -> id,
        'password   -> hashedPass
      ).executeUpdate
    }
  }
}