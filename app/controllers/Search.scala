package controllers

import models.SearchModel
import play.api.mvc._

object Search extends Controller with Secured {

  def index(page: Int, count: Int, query: String) = IsAuthenticated { implicit request =>

    val response = SearchModel.searchTicket(page, count, query)

    Ok(views.html.search.index(response)(request))
  }
}