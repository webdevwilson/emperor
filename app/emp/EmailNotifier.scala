package emp.plugin

import akka.actor.Actor
import emp._
import java.net.URL
import org.apache.commons.mail.HtmlEmail

class EmailNotifier extends Actor {

  def receive = {
    case d: EmperorEvent => {

      // Create the email message
      // val email = new HtmlEmail()
      // email.setHostName("smtp.gmail.com")
      // email.setTLS(true)
      // email.addTo("jheephat@gmail.com", "Cory Watson")
      // email.setFrom("jheephat@gmail.com", "Cory Watson")
      // email.setSubject("Test email with inline image")
      // email.setAuthentication("username", "password")

      // embed the image and get the content id
      // val url = new URL("http://www.apache.org/images/asf_logo_wide.gif")
      // val cid = email.embed(url, "Apache logo")

      // set the html message
      // email.setHtmlMsg("<html>The apache logo - <img src=\"cid:"+cid+"\"></html>")

      // set the alternative message
      // email.setTextMsg("Your email client does not support HTML messages")

      // send the email
      // email.send()
    }
  }
}

// XXX This should be a trait?
object EmailNotifier {

  def relevantEvents = List("ticket/changed", "ticket/created")
}