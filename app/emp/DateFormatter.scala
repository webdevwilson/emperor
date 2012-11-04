package emp

import controllers.AuthenticatedRequest
import java.text.SimpleDateFormat
import java.util.Date

import collection.JavaConversions._
import java.util.TimeZone


/**
 * Utilities for formatting dates.
 */
object DateFormatter {

  val longDateFormatter = new SimpleDateFormat("EEE, MMM d, yyyy")
  val longDateTimeFormatter = new SimpleDateFormat("HH:mm aa EEE, MMM d, yyyy")
  val iso8601Formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ")
  val timeZoneList = Seq(
    ("GMT"       -> "Greenwich Mean Time (GMT)"),
    ("GMT"       -> "Universal Coordinated Time (UTC)"),
    ("GMT+1:00"  -> "European Central Time (GMT+1)"),
    ("GMT+2:00"  -> "Eastern European Time (GMT+2)"),
    ("GMT+2:00"  -> "(Arabic) Egypt Standard Time (GMT+2)"),
    ("GMT+3:00"  -> "Eastern African Time (GMT+3)"),
    ("GMT+3:30"  -> "Middle East Time (GMT+3:30)"),
    ("GMT+4:00"  -> "Near East Time (GMT+4)"),
    ("GMT+5:00"  -> "Pakistan Lahore Time (GMT+5)"),
    ("GMT+5:30"  -> "India Standard Time (GMT+5:30)"),
    ("GMT+6:00"  -> "Bangladesh Standard Time (GMT+6)"),
    ("GMT+7:00"  -> "Vietnam Standard Time (GMT+7)"),
    ("GMT+8:00"  -> "China Taiwan Time (GMT+8)"),
    ("GMT+9:00"  -> "Japan Standard Time (GMT+9)"),
    ("GMT+9:30"  -> "Australia Central Time (GMT+9:30)"),
    ("GMT+10:00" -> "Australia Eastern Time (GMT+10)"),
    ("GMT+11:00" -> "Solomon Standard Time (GMT+11)"),
    ("GMT+12:00" -> "New Zealand Standard Time (GMT+12)"),
    ("GMT-11:00" -> "Midway Islands Time (GMT-11)"),
    ("GMT-10:00" -> "Hawaii Standard Time (GMT-10)"),
    ("GMT-9:00"  -> "Alaska Standard Time (GMT-9)"),
    ("GMT-8:00"  -> "Pacific Standard Time (GMT-8)"),
    ("GMT-7:00"  -> "Phoenix Standard Time (GMT-7"),
    ("GMT-7:00"  -> "Mountain Standard Time (GMT-7)"),
    ("GMT-6:00"  -> "Central Standard Time (GMT-6"),
    ("GMT-5:00"  -> "Eastern Standard Time (GMT-5)"),
    ("GMT-5:00"  -> "Indiana Eastern Standard Time (GMT-5"),
    ("GMT-4:00"  -> "Puerto Rico and US Virgin Islands Time (GMT-4)"),
    ("GMT-3:30"  -> "Canada Newfoundland Time (GMT-3:30)"),
    ("GMT-3:00"  -> "Argentina Standard Time (GMT-3)"),
    ("GMT-3:00"  -> "Brazil Eastern Time (GMT-3)"),
    ("GMT-1:00"  -> "Central African Time (GMT-1)")
  )

  // XXX We have the implicit request and can get the user's timezone here

  def getTimeZones: Seq[(String,String)] = {
    TimeZone.getAvailableIDs.map { tz => (tz, TimeZone.getTimeZone(tz).getDisplayName)
    }
  }

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