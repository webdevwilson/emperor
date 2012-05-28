package models

import anorm._
import anorm.SqlParser._
import chc._
import java.util.Date
import play.api.db.DB
import play.api.Play.current
import play.Logger

case class Workflow(id: Pk[Long] = NotAssigned, name: String, description: Option[String], dateCreated: Date)

case class WorkflowStatus(id: Pk[Long], workflowId: Long, statusId: Long, name: String, position: Int)

object WorkflowModel {

  val allQuery = SQL("SELECT * FROM workflows")
  val allStatuses = SQL("SELECT * FROM workflow_statuses ws JOIN ticket_statuses ts ON (ts.id = ws.status_id) WHERE workflow_id={id}")
  val getByIdQuery = SQL("SELECT * FROM workflows WHERE id={id}")
  val getWorkflowStatusByIdQuery = SQL("SELECT * FROM workflow_statuses ws JOIN ticket_statuses ts ON (ts.id = ws.status_id) WHERE ws.id={id}")
  val listQuery = SQL("SELECT * FROM workflows LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM workflows")
  val addQuery = SQL("INSERT INTO workflows (name, description, date_created) VALUES ({name}, {description}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE workflows SET name={name}, description={description} WHERE id={id}")
  val lastInsertQuery = SQL("SELECT LAST_INSERT_ID()")
  val getStartingStatus = SQL("SELECT * FROM workflow_statuses ws JOIN ticket_statuses ts ON ts.id = ws.status_id WHERE workflow_id={id} ORDER BY position ASC LIMIT 1")
  val getPrevStatus = SQL("SELECT * FROM workflow_statuses ws JOIN ticket_statuses ts ON (ts.id = ws.status_id) WHERE position < {position} AND workflow_id={workflow_id} ORDER BY position DESC LIMIT 1")
  val getNextStatus = SQL("SELECT * FROM workflow_statuses ws JOIN ticket_statuses ts ON (ts.id = ws.status_id) WHERE position > {position} AND workflow_id={workflow_id} ORDER BY position ASC LIMIT 1")
  val verifyStatusInWorkflow = SQL("SELECT count(*) FROM workflow_statuses WHERE status_id={status_id} AND workflow_id={workflow_id}")

  val workflow = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[Option[String]]("description") ~
    get[Date]("date_created") map {
      case id~name~description~dateCreated => Workflow(id, name, description, dateCreated)
    }
  }
  
  val workflowStatus = {
    get[Pk[Long]]("workflow_statuses.id") ~
    get[Long]("workflow_statuses.workflow_id") ~
    get[Long]("ticket_statuses.id") ~
    get[String]("ticket_statuses.name") ~
    get[Int]("workflow_statuses.position") map {
      case id~workflowId~statusId~name~position => WorkflowStatus(id, workflowId, statusId, name, position)
    }
  }
  
  def create(workflow: Workflow): Workflow = {

    DB.withConnection { implicit conn =>
      addQuery.on(
        'name         -> workflow.name,
        'description  -> workflow.description
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
      getByIdQuery.on('id -> id).as(workflow.singleOpt)
    }
  }

  def findStatusById(id: Long): Option[WorkflowStatus] = {
    
    DB.withConnection { implicit conn =>
      getWorkflowStatusByIdQuery.on('id -> id).as(workflowStatus.singleOpt)
    }
  }

  def findStatuses(id: Long) : Seq[WorkflowStatus] = {
    
    DB.withConnection { implicit conn =>
      allStatuses.on('id -> id).as(workflowStatus *)
    }
  }

  def getPreviousStatus(workflowStatusId: Long) : Option[WorkflowStatus] = {
    
    val ws = this.findStatusById(workflowStatusId)
    
    ws match {
      case Some(status) => {
        DB.withConnection { implicit conn =>

          getPrevStatus.on(
            'position   -> status.position,
            'workflow_id-> status.workflowId
          ).as(workflowStatus.singleOpt)
        }
      }
      case None => None
    }
  }

  def getNextStatus(workflowStatusId: Long) : Option[WorkflowStatus] = {
    
    val ws = this.findStatusById(workflowStatusId)
    
    ws match {
      case Some(status) => {
        DB.withConnection { implicit conn =>

          getNextStatus.on(
            'position   -> status.position,
            'workflow_id-> status.workflowId
          ).as(workflowStatus.singleOpt)
        }
      }
      case None => None
    }
  }

  def getStartingStatus(workflowId: Long) : Option[WorkflowStatus] = {
    
    DB.withConnection { implicit conn =>
      getStartingStatus.on('id -> workflowId).as(workflowStatus.singleOpt)
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
  
  /** Verify that given status is a member of the given workflow.
   *
   * Selects a count where status_id and workflow_id equal the given
   * arguments. Returns true if any are found, otherwise false.
   */
  def verifyStatusInWorkflow(workflowId: Long, statusId: Long) : Boolean = {
    DB.withTransaction { implicit conn =>
      val count = verifyStatusInWorkflow.on(
        'workflow_id-> workflowId,
        'status_id  -> statusId
      ).as(scalar[Long].single)
      
      count match {
        case 0 => false
        case _ => true
      }
    }
  }
}