package emp

import controllers.AuthenticatedRequest
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Utilities for formatting dates.
 */
object DateFormatter {

  val longDateFormatter = new SimpleDateFormat("EEE, MMM d, yyyy")
  val longDateTimeFormatter = new SimpleDateFormat("HH:mm aa EEE, MMM d, yyyy")
  val iso8601Formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ")

  // XXX We have the implicit request and can get the user's timezone here

  /**
   * Format a date into "long" date string in the form of `EEE, MMM d, yyyy`.
   * See also [[java.text.SimpleDateFormat]].
   */
  def displayLongDate(date: Date)(implicit request: AuthenticatedRequest): String = {
    longDateFormatter.format(date)
  }

  /**
   * Format a date into "long" date string in the form of `EEE, MMM d, yyyy`.
   * See also [[java.text.SimpleDateFormat]].
   */
  def displayLongDateTime(date: Date)(implicit request: AuthenticatedRequest): String = {
    longDateTimeFormatter.format(date)
  }

  def displayISO8601(date: Date)(implicit request: AuthenticatedRequest): String = {
    iso8601Formatter.format(date)
  }
}