package models

import anorm._
import anorm.SqlParser._
import chc._
import play.api.db.DB
import play.api.Play.current
import play.Logger

case class Workflow(id: Pk[Long] = NotAssigned, name: String, description: Option[String])

case class WorkflowStatus(id: Pk[Long], statusId: Long, name: String, position: Int)

object WorkflowModel {

  val allQuery = SQL("SELECT * FROM workflows")
  val allStatuses = SQL("SELECT ws.id, ts.id, ts.name, ws.position FROM workflow_statuses ws JOIN ticket_statuses ts ON (ts.id = ws.status_id) WHERE workflow_id={id}")
  val getByIdQuery = SQL("SELECT * FROM workflows WHERE id={id}")
  val getWorkflowStatusByIdQuery = SQL("SELECT * FROM workflow_status WHERE id={id}")
  val listQuery = SQL("SELECT * FROM workflows LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM workflows")
  val addQuery = SQL("INSERT INTO workflows (name, description) VALUES ({name}, {description})")
  val updateQuery = SQL("UPDATE workflows SET name={name}, description={description} WHERE id={id}")
  val lastInsertQuery = SQL("SELECT LAST_INSERT_ID()")
  val getStartingStatus = SQL("SELECT * FROM workflow_statuses ws JOIN ticket_statuses ts ON ts.id = ws.status_id WHERE workflow_id={id} ORDER BY position ASC LIMIT 1")

  val workflow = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[Option[String]]("description") map {
      case id~name~description => Workflow(id, name, description)
    }
  }
  
  val workflowStatus = {
    get[Pk[Long]]("workflow_statuses.id") ~
    get[Long]("ticket_statuses.id") ~
    get[String]("ticket_statuses.name") ~
    get[Int]("workflow_statuses.position") map {
      case id~statusId~name~position => WorkflowStatus(id, statusId, name, position)
    }
  }
  
  def create(workflow: Workflow): Workflow = {

    DB.withConnection { implicit conn =>
      addQuery.on(
        'name       -> workflow.name,
        'description-> workflow.description
      ).executeUpdate

      val id = lastInsertQuery.as(scalar[Long].single)

      workflow.copy(id = new Id(id))
    }
  }
  
  def delete(id: Long) {
      // XXX
  }

  def findById(id: Long) : Option[Workflow] = {
      
    DB.withConnection { implicit conn =>
      getByIdQuery.on('id -> id).as(WorkflowModel.workflow.singleOpt)
    }
  }

  def findWorkflowStatusById(id: Long): Option[WorkflowStatus] = {
    
    DB.withConnection { implicit conn =>
      getWorkflowStatusByIdQuery.on('id -> id).as(WorkflowModel.workflowStatus.singleOpt)
    }
  }

  def findStatuses(id: Long) : Seq[WorkflowStatus] = {
    
    DB.withConnection { implicit conn =>
      allStatuses.on('id -> id).as(workflowStatus *)
    }
  }

  def getPreviousStatus(ticket: models.Ticket) = {
    
    // DB.withConnection { implicit conn =>
    // 
    // }
  }

  def getStartingStatus(workflowId: Long) : Option[WorkflowStatus] = {
    
    DB.withConnection { implicit conn =>
      getStartingStatus.on('id -> workflowId).as(WorkflowModel.workflowStatus.singleOpt)
    }
  }

  def getAll: List[Workflow] = {
      
    DB.withConnection { implicit conn =>
      allQuery.as(workflow *)
    }
  }

  def list(page: Int = 0, count: Int = 10) : Page[Workflow] = {

      val offset = count * page
      
      DB.withConnection { implicit conn =>
        val workflows = listQuery.on(
          'count  -> count,
          'offset -> offset
        ).as(workflow *)

        val totalRows = listCountQuery.as(scalar[Long].single)

        Page(workflows, page, count, totalRows)
      }
  }
  
  def update(id: Long, workflow: Workflow) = {

    DB.withTransaction { implicit conn =>
      val foo = updateQuery.on(
        'id         -> id,
        'name       -> workflow.name,
        'description-> workflow.description
      ).executeUpdate
    }
  }
}