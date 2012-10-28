package emp.text

import org.pegdown.PegDownProcessor
import org.pegdown.Extensions

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
   * all the extensions turned on.
   */
  def render(markdown: Option[String]): String = {
    markdown.map({ m => renderer.markdownToHtml(m) }).getOrElse("")
  }
}