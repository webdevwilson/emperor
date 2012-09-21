package emp.plugin

import akka.actor.Actor
import emp._
import java.net.URL
import models.{TicketModel,UserModel}
import org.apache.commons.mail.HtmlEmail
import play.api.Configuration
import play.api.i18n.Messages

/**
 * Case class that defines the result of an event
 */
case class EmailResult(
  subject: String,
  recipient: models.User,
  body: String
)

class EmailNotifier(configuration: Configuration) extends Actor {

  val maybeSmtp     = configuration.getString("emperor.mail.smtp.server")
  val maybeFrom     = configuration.getString("emperor.mail.smtp.from.address")
  val maybeName     = configuration.getString("emperor.mail.smtp.from.name")
  val maybeTls      = configuration.getBoolean("emperor.mail.smtp.tls")
  val maybeUsername = configuration.getString("emperor.mail.smtp.username")
  val maybePassword = configuration.getString("emperor.mail.smtp.password")

  def receive = {
    case event: EmperorEvent => sendEmail(event)
  }

  def sendEmail(event: EmperorEvent) = {

    maybeSmtp.map { smtp =>
      val maybeResult: Option[EmailResult] = event match {
        // Handle a new ticket
        case nte: NewTicketEvent => {
          val ticket = TicketModel.getFullById(nte.ticketId).get

          // Only do this if we have an assignee to notify and XXX if the assignee
          // doesn't match the reporter. XXX should be configurable
          // XXX also for project owner or some other configurable list of project
          // watchers?
          ticket.assignee.id match {
            case Some(userId) => Some(EmailResult(
              subject = Messages("email.subject", Messages("email.subject.ticket.new", ticket.ticketId)),
              recipient = UserModel.getById(userId).get,
              body = views.html.email.notifier.ticket.newticket(ticket = ticket).body
            ))
            case None => None
          }
        }
        // Unknown event, just do nothing
        case _ => None
      }

      // If we got a result then send along an email.
      maybeResult.map { result =>
        // Create the email message
        val email = new HtmlEmail()
        email.setHostName(smtp)
        // TLS?
        maybeTls.map { tls =>
          email.setTLS(tls)
        }
        // Username & Password
        maybeUsername.map { username =>
          email.setAuthentication(username, maybePassword.get)
        }
        // To!
        email.addTo(result.recipient.email, result.recipient.realName)

        email.setFrom(maybeFrom.getOrElse("emperor@example.com"), maybeName.getOrElse("Emperor"))
        email.setSubject(result.subject)

        // set the html message
        email.setHtmlMsg(result.body)

        // set the alternative message
        email.setTextMsg("Your email client does not support HTML messages") // XXX

        // send the email
        email.send()
      }
    }
  }
}

// XXX This should be a trait?
object EmailNotifier {

  def relevantEvents = List("ticket/changed", "ticket/created")
}