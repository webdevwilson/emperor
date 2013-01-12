package emp.text

import org.pegdown.Extensions
import org.pegdown.{LinkRenderer,PegDownProcessor}

/**
 * Utility for hiding the actual rendering implementation. Currently supports
 * Markdown only.
 */
object Renderer {

  val renderer = new PegDownProcessor(Extensions.ALL)

  /**
   * Render a string &mdash; that is assumed to be Markdown &mdash; into
   * HTML.
   *
   * Uses [[https://github.com/sirthias/pegdown Pegdown]] for rendering with
   * all the extensions turned on and a customer link renderer via
   * [[emp.text.EmperorLinkRenderer]].
   *
   * @param markdown The text to render.
   */
  def render(markdown: Option[String]): String = {
    markdown.map({ m =>
      renderer.markdownToHtml(m, new EmperorLinkRenderer())
    }).getOrElse("")
  }
}