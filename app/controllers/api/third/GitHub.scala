package controllers.api.third

import emp.JsonFormats._
import controllers._
import models.{GroupModel,TicketModel,UserModel}
import play.api._
import play.api.mvc._
import play.api.libs.Jsonp
import play.api.libs.json.Json
import play.api.libs.json._

object GitHub extends Controller with Secured {

  val ticketFinder = "\\b(\\p{L}{1}[\\p{Nd}|\\p{L}]*-\\d+)\\b".r

  /**
   * Note a commit to a repo for a project
   */
  def commit = Action { implicit request =>

    request.body.asFormUrlEncoded.map { data =>
      data.get("payload").map { payload =>
        val ghcommits = Json.parse(payload.head)
        (ghcommits \ "commits") match {
          case JsArray(commits) => commits.foreach({ commit =>
            // Get the SHA and URL so we can add a link to the text
            val sha = (commit \ "id").as[String].substring(0, 10) // Just the ifrst 10
            val url = (commit \ "url").as[String]
            (commit \ "message").as[Option[String]].map({ message =>
              // Put [[]] around ticket-looking things so the wiki linker picks them up
              val repl = ticketFinder.replaceAllIn(message, m => "[[" + m.group(0) + "]]")
              ticketFinder.findAllIn(message).foreach({ t =>
                TicketModel.addComment(
                  ticketId = t,
                  ctype = "commit",
                  userId = 1,
                  // Make content with a link to the URL
                  content = "[" + sha + "](" + url + "): " + repl
                )
                println("added comment")
              })
            })
          })
          case _ => {
            // Uhm... Nothing to do!
          }
        }
        Ok("ok")
      }.getOrElse(BadRequest("Expected JSON payload"))
    }.getOrElse(BadRequest("Expected JSON payload"))
  }
}