package controllers.api.third

import emp.JsonFormats._
import controllers._
import models.{GroupModel,TicketModel,UserModel}
import play.api._
import play.api.mvc._
import play.api.libs.Jsonp
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.Play.current

object GitHub extends Controller with Secured {

  val ticketFinder = "\\b(\\p{L}{1}[\\p{Nd}|\\p{L}]*-\\d+)\\b".r

  /**
   * Note a commit to a repo for a project
   */
  def commit = IsAuthenticated() { implicit request =>

    // XXX This should be a plugin
    request.body.asFormUrlEncoded.map { data =>
      data.get("payload").map { payload =>
        val ghcommits = Json.parse(payload.head)
        (ghcommits \ "commits") match {
          case JsArray(commits) => commits.foreach({ commit =>
            // Get the committers email
            val committer = (commit \ "committer" \ "email").as[String]
            UserModel.getByEmail(committer).flatMap({ user =>
              // Get the SHA and URL so we can add a link to the text
              val sha = (commit \ "id").as[String].substring(0, 10) // Just the first 10
              val url = (commit \ "url").as[String]
              (commit \ "message").as[Option[String]].map({ message =>
                // Put [[]] around ticket-looking things so the wiki linker picks them up
                val repl = ticketFinder.replaceAllIn(message, m => "[[" + m.group(0) + "]]")
                ticketFinder.findAllIn(message).foreach({ t =>
                  TicketModel.getActualId(t).map({ tid =>
                    TicketModel.addComment(
                      ticketId = tid,
                      ctype = "commit",
                      userId = user.id.get,
                      // Make content with a link to the URL
                      content = "[" + sha + "](" + url + "): " + repl
                    )
                    Logger.debug("Added GitHub comment to " + t)
                  })
                })
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