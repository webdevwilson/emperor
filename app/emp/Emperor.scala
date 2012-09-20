package emp

import play.api._
import play.core._

class EmperorPlugin(app: Application) extends Plugin {

  override def onStart {

    println("")
    println("")
    println("################ asdasd!")
    println(app.configuration)
    println("")
    println("")
    throw NeedSetup()
  }
}

case class NeedSetup() extends PlayException("NEED SETUP", "ASDASD", None)
  with PlayException.ExceptionAttachment with PlayException.RichDescription {

  def subTitle = "This is a subtitle"

  def content = "This is content"

  def htmlDescription: String = {
    <h1>Need to make your db.</h1>
  }.map(_.toString).mkString
}