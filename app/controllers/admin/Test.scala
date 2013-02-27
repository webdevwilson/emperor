package controllers.admin

import anorm._
import emp._
import controllers._
import de.svenjacobs.loremipsum.LoremIpsum
import java.util.Random
import models._
import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.json.Json._
import play.api.mvc._
import play.api.db._

object Test extends Controller with Secured {

  def generate = IsAuthenticated(perm = "PERM_GLOBAL_ADMIN") { implicit request =>

    val projects = List(
      ("Campaign", "CAMP"),
      ("Scouting", "SCOUT"),
      ("FrontOffice", "FO"),
      ("Philanthropy", "PHIL")
    )

    val fakeProjects = projects.map { project =>
      ProjectModel.create(models.Project(
        workflowId = 1, name = project._1,
        ownerId = None, permissionSchemeId = 1, defaultPriorityId = None,
        defaultSeverityId = None, defaultTypeId = None, defaultAssignee = None,
        key = project._2, dateCreated = new DateTime()
      ))
    }

    val users = List(
      ("Arsène Wenger", "arsene"),
      ("Wojciech Szczęsny", "wojciech"),
      ("Abou Diaby", "abou"),
      ("Bacary Sagna", "bacary"),
      ("Per Mertesacker", "per"),
      ("Thomas Vermaelen", "thomas"),
      ("Laurent Koscielny", "laurent"),
      ("Tomáš Rosický", "tomas"),
      ("Mikel Arteta", "mikel"),
      ("Park Chu-Young", "park"),
      ("Robin van Persie", "rvp"),
      ("André Santos", "andre"),
      ("Olivier Giroud", "olivier"),
      ("Theo Walcott", "theo"),
      ("Alex Oxlade-Chamberlain", "ox"),
      ("Aaron Ramsey", "aaron"),
      ("Alex Song", "alex"),
      ("Sébastien Squillaci", "sebastient"),
      ("Jack Wilshere", "jack"),
      ("Johan Djourou", "johan"),
      ("Łukasz Fabiański", "lukasz"),
      ("Francis Coquelin", "franics"),
      ("Andrei Arshavin", "andrei"),
      ("Vito Mannone", "vito"),
      ("Carl Jenkinson", "carl"),
      ("Emmanuel Frimpong", "emmanuel"),
      ("Gervinho", "gervinho"),
      ("Kieran Gibbs", "kieran"),
      ("Marouane Chamakh", "marouane"),
      ("Ryo Miyaichi", "ryo"),
      ("Kyle Bartley", "kyle"),
      ("Nicklas Bendtner", "nicklas"),
      ("Henri Lansbury", "henri"),
      ("Lukas Podolski", "lukas")
    )

    val fakeUsers = users.map { user =>
      UserModel.create(models.User(
        username = user._2, password = "test", realName = user._1, email = user._2 + "@example.com", organization = None, location = None, title = None, url = None, timezone = "GMT-6:00", dateCreated = new DateTime()
      ))
    }

    val groups = List(
      "First Squad", "Substitutes", "Reserve Squad", "Out on Loan", "Forwards", "Midfielders", "Fullbacks", "Goalies", "Strikers"
    )
    val fakeGroups = groups.map { group =>
      GroupModel.create(models.Group(
        name = group, dateCreated = new DateTime()
      ))
    }

    val rand = new Random(System.currentTimeMillis())
    val textGen = new LoremIpsum()

    val tickPrios = List(1, 2, 3)
    val tickSevs = List(1, 2, 3)
    val tickTypes = List(1, 2, 3)

    val fakeTickets = 1.to(100) map { index =>
      val summWords = rand.nextInt(30)
      TicketModel.create(
        userId = fakeUsers(rand.nextInt(fakeUsers.size)).id.get,
        ticket = InitialTicket(
          reporterId = fakeUsers(rand.nextInt(fakeUsers.size)).id.get,
          assigneeId = rand.nextInt(2) match {
            // Some should be unassigned
            case 0 => None
            // Some assigned
            case 1 => Some(fakeUsers(rand.nextInt(fakeUsers.size)).id.get)
          },
          projectId = fakeProjects(rand.nextInt(fakeProjects.size)).id.get,
          priorityId = tickPrios(rand.nextInt(tickPrios.size)).toLong,
          severityId = tickSevs(rand.nextInt(tickSevs.size)).toLong,
          typeId = tickTypes(rand.nextInt(tickTypes.size)).toLong,
          position = None,
          summary = textGen.getWords(summWords, summWords - rand.nextInt(10) match {
            case x if x < 0 => 0
            case y => y
          }),
          description = rand.nextInt(4) match {
            case 1 => None
            case _ => Some(textGen.getWords(rand.nextInt(100)))
          }
        )
      )
    }

    Redirect(controllers.routes.Admin.index).flashing("success" -> "admin.test.generate.success")
  }
}
