package emp.text

import models.{TicketModel,UserModel}
import org.pegdown.LinkRenderer
import org.pegdown.ast.WikiLinkNode

/**
 * Implements a customer LinkRenderer that interprets wiki-style links
 * &mdash; those surrounded with double brackets &mdash; and attempts to turn them into
 * something useful. Currently groks @ usernames and things that look like
 * tickets.  Ticket or users that do not actually exist will show up with the
 * `text-error` class.
 */
class EmperorLinkRenderer extends LinkRenderer {

  /**
   * Interpret a WikiLinkNode into a useful link.
   */
  override def render(node: WikiLinkNode): LinkRenderer.Rendering = {

    if(TicketModel.isValidTicketId(node.getText)) {
      // Doesn't mean that the ticket is valid, just that the form is correct.
      // val maybeTicket = TicketModel.getById(node.getText())
      // maybeTicket.map(ticket =>
        // new LinkRenderer.Rendering("/ticket/" + ticket.ticketId.get, ticket.ticketId.get)
        new LinkRenderer.Rendering("/ticket/" + node.getText, node.getText)
      // ).getOrElse(
      //   // Ticket doesn't exist, show a useless ticket link.
      //   new LinkRenderer.Rendering("/", node.getText()).withAttribute("class", "text-error")
      // )
    } else if(node.getText.startsWith("@")) {
      val maybeUser = UserModel.getByUsername(node.getText.substring(1))
      maybeUser.map(user =>
        new LinkRenderer.Rendering("mailto:" + user.email, user.realName)
      ).getOrElse(
        // User doesn't exist, show a useless link.
        new LinkRenderer.Rendering("/", node.getText()).withAttribute("class", "text-error")
      )
    } else {
      // Link to /, this does nothing.
      new LinkRenderer.Rendering("/", node.getText()).withAttribute("class", "text-error")
    }
  }
}