package emp.text

import org.pegdown.PegDownProcessor
import org.pegdown.Extensions

object Renderer {

  val renderer = new PegDownProcessor(Extensions.ALL)

  def render(markdown: Option[String]): String = {
    markdown.map({ m => renderer.markdownToHtml(m) }).getOrElse("")
  }
}