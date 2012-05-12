package models

import anorm._
import anorm.SqlParser._
import chc._
import play.api.db.DB
import play.api.Play.current

case class Project(id: Pk[Long] = NotAssigned, name: String, key: String)

object ProjectModel {

  val allQuery = SQL("SELECT * FROM projects")
  val getByIdQuery = SQL("SELECT * FROM projects WHERE id={id}")
  val listQuery = SQL("SELECT * FROM projects LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM projects")
  val insertQuery = SQL("INSERT INTO projects (name, pkey) VALUES ({name}, {pkey})")
  val updateQuery = SQL("UPDATE projects SET name={name} WHERE id={id}")

  val project = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[String]("pkey") map {
      case id~name~pkey => Project(id, name, pkey)
    }
  }

  def create(project: Project): Project = {

    DB.withConnection { implicit conn =>
      insertQuery.on(
        'name   -> project.name,
        'pkey   -> project.key
      ).executeUpdate
    }
    
    project
  }
  
  def delete(id: Long) {
      
  }

  def findById(id: Long) : Option[Project] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(ProjectModel.project.singleOpt)
    }
  }

  def getAll: List[Project] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(project *)
    }
  }

  def list(page: Int = 0, count: Int = 10) : Page[Project] = {

      val offset = count * page
      
      DB.withConnection { implicit conn =>
        val projects = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(project *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(projects, page, count, totalRows)
      }
  }
  
  def update(id: Long, project: Project) = {

    DB.withTransaction { implicit conn =>
      val foo = updateQuery.on(
        'id         -> id,
        'name       -> project.name
      ).executeUpdate
    }
  }
}