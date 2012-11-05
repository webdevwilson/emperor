package models

import emp._
import org.joda.time.DateTime
import play.Logger

/**
 * Class for events.
 */
case class Event(
  projectId: Long,
  projectName: String,
  userId: Long,
  userRealName: String,
  eKey: String,
  eType: String,
  content: String,
  url: String,
  dateCreated: DateTime
)

object EventModel {
}
