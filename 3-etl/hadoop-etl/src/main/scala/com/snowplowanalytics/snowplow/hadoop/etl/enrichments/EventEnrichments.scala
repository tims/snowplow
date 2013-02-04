/*
 * Copyright (c) 2012-2013 SnowPlow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics.snowplow.hadoop.etl
package enrichments

// Java
import java.util.UUID

// Scalaz
import scalaz._
import Scalaz._

// Joda-Time
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Holds the enrichments related to events.
 */
object EventEnrichments {

  /**
   * The Tracker Protocol's pattern
   * for a timestamp - for details
   * see:
   *
   * https://github.com/snowplow/snowplow/wiki/snowplow-tracker-protocol#wiki-common-params
   */
  private val TstampFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH-mm-ss")
  private val DtFormat = DateTimeFormat.forPattern("yyyy-MM-dd")
  private val TmFormat = DateTimeFormat.forPattern("HH-mm-ss")  

  /**
   * Splits a Joda DateTime into
   * separate time and date Strings
   *
   * @param datetime The Joda DateTime
   *        to split into two
   * @return a Tuple of two Strings
   *         (date and time)
   */
  def splitDatetime(datetime: DateTime): DateTimeTuple =
    (DtFormat.print(datetime), TmFormat.print(datetime))

  /**
   * Extracts the timestamp from the
   * format as laid out in the Tracker
   * Protocol:
   *
   * https://github.com/snowplow/snowplow/wiki/snowplow-tracker-protocol#wiki-common-params
   *
   * @param tstamp The timestamp as
   *        stored in the Tracker
   *        Protocol
   * @return a Tuple of two Strings
   *         (date and time), or an
   *         error message if the
   *         format was invalid
   */
  def extractTimestamp(tstamp: String): Validation[String, DateTimeTuple] = try {
      val t = TstampFormat.parseDateTime(tstamp)
      splitDatetime(t).success
    } catch {
      case iae: IllegalArgumentException =>
        "[%s] is not in the expected timestamp format".format(tstamp).fail
    }

  /**
   * Turns an event code into a valid event type,
   * e.g. "pv" -> "page_view". See the Tracker
   * Protocol for details:
   *
   * https://github.com/snowplow/snowplow/wiki/snowplow-tracker-protocol#wiki-event2
   *
   * @param eventCode The event code
   * @return the event type, or an error message
   *         if not recognised, boxed in a Scalaz
   *         Validation
   */
  def extractEventType(eventCode: String): Validation[String, String] = eventCode match {
    case "ev" => "custom".success
    case "ad" => "ad_impression".success
    case "tr" => "transaction".success
    case "ti" => "transaction_item".success
    case "pv" => "page_view".success
    case "pp" => "page_ping".success
    case  ec  => "[%s] is not a recognised event code".format(ec).fail
  }

  /**
   * Returns a unique event ID. The event ID is 
   * generated as a type 4 UUID, then converted
   * to a String.
   *
   * () on the function signature because it's
   * not pure
   *
   * @return the unique event ID
   */
  def generateEventId(): String = UUID.randomUUID().toString
}