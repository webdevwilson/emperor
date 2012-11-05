package emp

import controllers.AuthenticatedRequest
import collection.JavaConversions._

import org.joda.time.format.{DateTimeFormat,DateTimeFormatter,ISODateTimeFormat}
import org.joda.time.{DateTime,DateTimeZone}

/**
 * Utilities for formatting dates.
 */
object DateFormatter {

  val longDateFormatter = DateTimeFormat.forPattern("EEE, MMM d, yyyy")
  val longDateTimeFormatter = DateTimeFormat.forPattern("HH:mm aa EEE, MMM d, yyyy")
  val isoFormatter = ISODateTimeFormat.dateTime()

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

  def displayLongDateTime(dt: DateTime): String = {
    longDateTimeFormatter.print(dt.withZone(DateTimeZone.forID("America/Chicago")))
  }

  def displayISO8601(dt: DateTime): String = isoFormatter.print(dt.withZone(DateTimeZone.forID("America/Chicago")))
}