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

  val realipHeader = Play.configuration.getConfig("emperor").get.getString("ip-header")
  val ticketFinder = "\\b(\\p{L}{1}[\\p{Nd}|\\p{L}]*-\\d+)\\b".r
  val githubAddresses = Seq("207.97.227.253", "50.57.128.197", "108.171.174.178")

  /**
   * Note a commit to a repo for a project
   */
  def commit = Action { implicit request =>

    // Make sure this request came from github. This is a temporary hack
    // until it's worth asking GitHub to add support for Emperor's tokens.
    val ip = realipHeader.flatMap({ headerName => request.headers.get(headerName) }).getOrElse(request.remoteAddress)
    if(githubAddresses.contains(ip)) {
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
                    TicketModel.addComment(
                      ticketId = t,
                      ctype = "commit",
                      userId = user.id.get,
                      // Make content with a link to the URL
                      content = "[" + sha + "](" + url + "): " + repl
                    )
                    println("added comment " + t)
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
    } else {
      println("BAD IP: " + request.remoteAddress)
      BadRequest("Not a GitHub IP")
    }
  }
}