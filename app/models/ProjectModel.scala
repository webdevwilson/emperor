package models

import anorm._
import anorm.SqlParser._
import chc._
import java.util.Date
import play.api.db.DB
import play.api.Play.current

case class Project(id: Pk[Long] = NotAssigned, workflowId: Long, name: String, key: String, dateCreated: Date)

object ProjectModel {

  val allQuery = SQL("SELECT * FROM projects")
  val getByIdQuery = SQL("SELECT * FROM projects WHERE id={id}")
  val getByWorkflow = SQL("SELECT * FROM projects WHERE workflow_id={workflow_id}")
  val listQuery = SQL("SELECT * FROM projects LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM projects")
  val insertQuery = SQL("INSERT INTO projects (name, pkey, workflow_id, date_created) VALUES ({name}, {pkey}, {workflow_id}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE projects SET name={name}, workflow_id={workflow_id} WHERE id={id}")
  val lastInsertQuery = SQL("SELECT LAST_INSERT_ID()")

  val project = {
    get[Pk[Long]]("id") ~
    get[Long]("workflow_id") ~
    get[String]("name") ~
    get[String]("pkey") ~
    get[Date]("date_created") map {
      case id~workflowId~name~pkey~dateCreated => Project(id, workflowId, name, pkey, dateCreated)
    }
  }

  def create(project: Project): Project = {

    DB.withConnection { implicit conn =>
      insertQuery.on(
        'name         -> project.name,
        'pkey         -> project.key,
        'workflow_id  -> project.workflowId
      ).executeUpdate

      val id = lastInsertQuery.as(scalar[Long].single)
      this.getById(id).get
    }
  }
  
  def delete(id: Long) {
      
  }

  def getById(id: Long) : Option[Project] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(project.singleOpt)
    }
  }

  def getAll: List[Project] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(project *)
    }
  }

  def getWithWorkflow(id: Long) : Seq[Project] = {
    
    DB.withConnection { implicit conn => 
      getByWorkflow.on('workflow_id -> id).as(project *)
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
        'name       -> project.name,
        'workflow_id-> project.workflowId
      ).executeUpdate
    }
  }
}