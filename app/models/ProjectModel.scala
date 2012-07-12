package models

import anorm._
import anorm.SqlParser._
import chc._
import java.util.Date
import play.api.db.DB
import play.api.Play.current

case class Project(
  id: Pk[Long] = NotAssigned, workflowId: Long, sequenceCurrent: Long = 0,
  name: String, key: String, dateCreated: Date
)

object ProjectModel {

  val allQuery = SQL("SELECT * FROM projects")
  val getByIdQuery = SQL("SELECT * FROM projects WHERE id={id}")
  val getByWorkflowQuery = SQL("SELECT * FROM projects WHERE workflow_id={workflow_id}")
  val updateSequenceQuery = SQL("UPDATE projects SET sequence_current = LAST_INSERT_ID(sequence_current + 1) WHERE id={id}")
  val listQuery = SQL("SELECT * FROM projects LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM projects")
  val insertQuery = SQL("INSERT INTO projects (name, pkey, workflow_id, date_created) VALUES ({name}, {pkey}, {workflow_id}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE projects SET name={name}, workflow_id={workflow_id} WHERE id={id}")
  val deleteQuery = SQL("DELETE FROM projects WHERE id={id}")

  val project = {
    get[Pk[Long]]("id") ~
    get[Long]("workflow_id") ~
    get[Long]("sequence_current") ~
    get[String]("name") ~
    get[String]("pkey") ~
    get[Date]("date_created") map {
      case id~workflowId~seqCurr~name~pkey~dateCreated => Project(
        id = id, workflowId = workflowId, name = name, key = pkey,
        sequenceCurrent = seqCurr, dateCreated = dateCreated
      )
    }
  }

  /**
   * Create a project.
   */
  def create(project: Project): Project = {

    DB.withConnection { implicit conn =>
      val id = insertQuery.on(
        'name         -> project.name,
        'pkey         -> project.key,
        'workflow_id  -> project.workflowId
      ).executeInsert()

      this.getById(id.get).get
    }
  }

  /**
   * Delete project.
   */
  def delete(id: Long) {
    DB.withConnection { implicit conn =>
      deleteQuery.on('id -> id).execute
    }
  }

  /**
   * Get a project by id.
   */
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

  /**
   * Increment this project's sequence and return the new
   * value.  The operation is atomic.
   */
  def getNextSequence(id: Long) : Option[Long] = {

    DB.withConnection { implicit conn =>
      updateSequenceQuery.on('id -> id).executeInsert()
    }
  }

  def getWithWorkflow(id: Long) : Seq[Project] = {

    DB.withConnection { implicit conn =>
      getByWorkflowQuery.on('workflow_id -> id).as(project *)
    }
  }

  def list(page: Int = 1, count: Int = 10) : Page[Project] = {

      val offset = count * (page - 1)

      DB.withConnection { implicit conn =>
        val projects = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(project *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(projects, page, count, totalRows)
      }
  }

  /**
   * Update a project.
   */
  def update(id: Long, project: Project): Option[Project] = {

    DB.withConnection { implicit conn =>
      val foo = updateQuery.on(
        'id         -> id,
        'name       -> project.name,
        'workflow_id-> project.workflowId
      ).execute
      getById(id)
    }
  }
}
