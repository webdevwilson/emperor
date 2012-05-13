package models

import anorm._
import anorm.SqlParser._
import chc._
import play.api.db.DB
import play.api.Play.current

case class Project(id: Pk[Long] = NotAssigned, workflowId: Long, name: String, key: String)

object ProjectModel {

  val allQuery = SQL("SELECT * FROM projects")
  val getByIdQuery = SQL("SELECT * FROM projects WHERE id={id}")
  val getByWorkflow = SQL("SELECT * FROM projects WHERE workflow_id={workflow_id}")
  val listQuery = SQL("SELECT * FROM projects LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM projects")
  val insertQuery = SQL("INSERT INTO projects (name, pkey, workflow_id) VALUES ({name}, {pkey}, {workflow_id})")
  val updateQuery = SQL("UPDATE projects SET name={name}, workflow_id={workflow_id} WHERE id={id}")

  val project = {
    get[Pk[Long]]("id") ~
    get[Long]("workflow_id") ~
    get[String]("name") ~
    get[String]("pkey") map {
      case id~workflowId~name~pkey => Project(id, workflowId, name, pkey)
    }
  }

  def create(project: Project): Project = {

    DB.withConnection { implicit conn =>
      insertQuery.on(
        'name         -> project.name,
        'pkey         -> project.key,
        'workflow_id  -> project.workflowId
      ).executeUpdate
    }
    
    project
  }
  
  def delete(id: Long) {
      
  }

  def findById(id: Long) : Option[Project] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(project.singleOpt)
    }
  }

  def getAll: List[Project] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(project *)
    }
  }

  def findWithWorkflow(id: Long) : Seq[Project] = {
    
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