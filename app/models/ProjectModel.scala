package models

import anorm._
import anorm.SqlParser._
import emp.util.AnormExtension._
import emp.event._
import emp.util.Pagination.Page
import org.joda.time.DateTime
import java.util.regex.Pattern
import play.api.db.DB
import play.api.Play.current

/**
 * Class for a project.
 */
case class Project(
  id: Pk[Long] = NotAssigned,
  workflowId: Long,
  sequenceCurrent: Long = 0,
  name: String,
  key: String,
  ownerId: Option[Long],
  permissionSchemeId: Long,
  defaultPriorityId: Option[Long],
  defaultSeverityId: Option[Long],
  defaultTypeId: Option[Long],
  defaultAssignee: Option[Int],
  dateCreated: DateTime
)

object DefaultAssignee extends Enumeration {
  type DefaultAssignee = Value
  val Def_Assign_Nobody, Def_Assign_Owner = Value
}

object ProjectModel {

  val allQuery = SQL("SELECT * FROM projects WHERE pkey != 'EMPCORE'")
  val allVisibleProjectsQuery = SQL("SELECT p.* FROM full_permissions AS fp JOIN projects p ON p.id = fp.project_id WHERE user_id={user_id} AND permission_id IN ('PERM_PROJECT_ADMIN', 'PERM_PROJECT_BROWSE', 'PERM_GLOBAL_ADMIN') AND project_key != 'EMPCORE'")
  val getAllVisibleProjectIdsQuery = SQL("SELECT p.id FROM full_permissions AS fp JOIN projects p ON p.id = fp.project_id WHERE user_id={user_id} AND permission_id IN ('PERM_PROJECT_ADMIN', 'PERM_PROJECT_BROWSE', 'PERM_GLOBAL_ADMIN') AND project_key != 'EMPCORE'")
  val getByIdQuery = SQL("SELECT * FROM projects WHERE id={id}")
  val getByKeyQuery = SQL("SELECT * FROM projects WHERE pkey={pkey}")
  val getByWorkflowQuery = SQL("SELECT * FROM projects WHERE workflow_id={workflow_id} AND pkey != 'EMPCORE'")
  val updateSequenceQuery = SQL("UPDATE projects SET sequence_current = LAST_INSERT_ID(sequence_current + 1) WHERE id={id}")
  val listQuery = SQL("SELECT p.* FROM full_permissions AS fp JOIN projects p ON p.id = fp.project_id WHERE user_id={user_id} AND permission_id IN ('PERM_PROJECT_BROWSE', 'PERM_GLOBAL_ADMIN') AND project_key != 'EMPCORE' LIMIT {offset},{count}")
  val listCountQuery = SQL("SELECT count(*) FROM projects WHERE pkey != 'EMPCORE'")
  val insertQuery = SQL("INSERT INTO projects (name, pkey, workflow_id, owner_id, permission_scheme_id, default_priority_id, default_severity_id, default_ticket_type_id, default_assignee, date_created) VALUES ({name}, {pkey}, {workflow_id}, {owner_id}, {permission_scheme_id}, {default_priority_id}, {default_severity_id}, {default_ticket_type_id}, {default_assignee}, UTC_TIMESTAMP())")
  val updateQuery = SQL("UPDATE projects SET name={name}, workflow_id={workflow_id}, owner_id={owner_id}, permission_scheme_id={permission_scheme_id}, default_priority_id={default_priority_id}, default_severity_id={default_severity_id}, default_ticket_type_id={default_ticket_type_id}, default_assignee={default_assignee} WHERE id={id}")
  val deleteQuery = SQL("DELETE FROM projects WHERE id={id}")

  // Parser for retrieving a project.
  val project = {
    get[Pk[Long]]("id") ~
    get[Long]("workflow_id") ~
    get[Long]("sequence_current") ~
    get[String]("name") ~
    get[String]("pkey") ~
    get[Option[Long]]("owner_id") ~
    get[Long]("permission_scheme_id") ~
    get[Option[Long]]("default_priority_id") ~
    get[Option[Long]]("default_severity_id") ~
    get[Option[Long]]("default_ticket_type_id") ~
    get[Option[Int]]("default_assignee") ~
    get[DateTime]("date_created") map {
      case id~workflowId~seqCurr~name~pkey~ownerId~permId~defPrioId~defSevId~defTypeId~defAss~dateCreated => Project(
        id = id, workflowId = workflowId, name = name, key = pkey,
        sequenceCurrent = seqCurr, permissionSchemeId = permId, ownerId = ownerId,
        defaultPriorityId = defPrioId, defaultSeverityId = defSevId,
        defaultTypeId = defTypeId, defaultAssignee = defAss,
        dateCreated = dateCreated
      )
    }
  }

  val keyPattern = Pattern.compile("^\\p{L}{1}[\\p{Nd}|\\p{L}]*")

  /**
   * Verifies that a string is a valid project key via regex.
   */
  def isValidKey(key: String): Boolean = keyPattern.matcher(key).matches

  /**
   * Create a project.
   */
  def create(project: Project): Project = {

    DB.withConnection { implicit conn =>
      val id = insertQuery.on(
        'name         -> project.name,
        'pkey         -> project.key,
        'workflow_id  -> project.workflowId,
        'owner_id     -> project.ownerId,
        'permission_scheme_id -> project.permissionSchemeId,
        'default_priority_id -> project.defaultPriorityId,
        'default_severity_id -> project.defaultSeverityId,
        'default_ticket_type_id -> project.defaultTypeId,
        'default_assignee -> project.defaultAssignee
      ).executeInsert()

      id.map { pid =>
        EmperorEventBus.publish(
          NewProjectEvent(
            projectId = pid
          )
        )
      }

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
   * Get all visible projects for a user
   */
  def getAll(userId: Long): List[Project] = {
    DB.withConnection { implicit conn =>
      allVisibleProjectsQuery.on('user_id -> userId).as(project *)
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

  /**
   * Get a project by id.
   */
  def getByKey(pkey: String) : Option[Project] = {

    DB.withConnection { implicit conn =>
      getByKeyQuery.on('pkey -> pkey).as(project.singleOpt)
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

  def getVisibleProjectIds(userId: Long): List[Long] = {
    DB.withConnection { implicit conn =>
      getAllVisibleProjectIdsQuery.on('user_id -> userId).as(long("id") *)
    }
  }

  def getWithWorkflow(id: Long): Seq[Project] = {

    DB.withConnection { implicit conn =>
      getByWorkflowQuery.on('workflow_id -> id).as(project *)
    }
  }

  def list(userId: Long, page: Int = 1, count: Int = 10) : Page[Project] = {

      val offset = count * (page - 1)

      DB.withConnection { implicit conn =>
        val projects = allVisibleProjectsQuery.on(
          'user_id -> userId,
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
      updateQuery.on(
        'id         -> id,
        'name       -> project.name,
        'workflow_id-> project.workflowId,
        'owner_id   -> project.ownerId,
        'permission_scheme_id -> project.permissionSchemeId,
        'default_priority_id -> project.defaultPriorityId,
        'default_severity_id -> project.defaultSeverityId,
        'default_ticket_type_id -> project.defaultTypeId,
        'default_assignee -> project.defaultAssignee
      ).execute

      EmperorEventBus.publish(
        ChangeProjectEvent(
          projectId = id
        )
      )

      getById(id)
    }
  }
}
