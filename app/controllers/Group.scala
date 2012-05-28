package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import com.codahale.jerkson.Json._
import models.GroupModel

object Group extends Controller with Secured {

  def startsWith() = IsAuthenticated { implicit request =>

    val param = request.queryString.get("query");
    
    param match {
      case Some(query) => Ok(generate(GroupModel.getStartsWith(query.head)))
      case None => NotFound
    }    
  }
}