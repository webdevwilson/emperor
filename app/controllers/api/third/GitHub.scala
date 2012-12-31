package controllers.api.third

import emp.JsonFormats._
import controllers._
import models.{GroupModel,UserModel}
import play.api._
import play.api.mvc._
import play.api.libs.Jsonp
import play.api.libs.json.Json
import play.api.libs.json._

object GitHub extends Controller with Secured {

  val ticketFinder = "\\b\\p{L}{1}[\\p{Nd}|\\p{L}]*-\\d+\\b".r

  /**
   * Note a commit to a repo for a project
   */
  def commit = Action { implicit request =>

    request.body.asFormUrlEncoded.map { data =>
      data.get("payload").map { payload =>
        val load = Json.parse(payload.head)
        (load \ "commits") match {
          case JsArray(commits) => commits.foreach({ commit =>
            (commit \ "message").as[Option[String]].map({ message =>
              ticketFinder.findAllIn(message).foreach({ t =>
                println("T: " + t)
              })
            })
          })
          case _ => {
            // Uhm...
          }
        }
        Ok("ok")
      }.getOrElse(BadRequest("Expected JSON payload"))
    }.getOrElse(BadRequest("Expected JSON payload"))
  }
}