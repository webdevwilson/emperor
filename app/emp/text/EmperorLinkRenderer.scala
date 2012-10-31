package emp.text

import org.pegdown.LinkRenderer
import org.pegdown.ast.WikiLinkNode

/**
 * Implements a customer LinkRenderer that interprets wiki-style links
 * &mdash; those surrounded with [[ ]] &mdash; and attempts to turn them into
 * something useful. Currently groks @ signs with usernames and things that
 * look like tickets.
 */
class EmperorLinkRenderer extends LinkRenderer {

  /**
   * Interpret a WikiLinkNode into a useful link.
   */
  override def render(node: WikiLinkNode): LinkRenderer.Rendering = {

    if(node.getText.startsWith("@")) {
      // This is a reference to a username. XXX Links to nothing and doesn't check for validity.
      new LinkRenderer.Rendering("/user/" + node.getText().substring(1), node.getText())
    } else {
      // Assume it's a ticket reference. XXX Need to define what that is.
      new LinkRenderer.Rendering("/ticket/" + node.getText(), node.getText())
    }

  }
}