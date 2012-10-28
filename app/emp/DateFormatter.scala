package emp

import java.text.SimpleDateFormat
import java.util.Date

/**
 * Utilities for formatting dates.
 */
object DateFormatter {

  val longFormatter = new SimpleDateFormat("EEE, MMM d, yyyy")

  /**
   * Format a date (in seconds) into "long" string in the form of `EEE, MMM d, yyyy`.
   * See also [[java.text.SimpleDateFormat]].
   */
  def displayLongDate(timestamp: Long): String = {
    longFormatter.format(new Date(timestamp * 1000))
  }
}