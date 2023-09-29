package uk.gov.hmrc.testreport

import java.text.SimpleDateFormat
import java.util.Date

object DataFormatter {
  def getOrdinalSuffix(day: Int): String =
    if (day % 10 == 1 && day != 11) {
      "st"
    } else if (day % 10 == 2 && day != 12) {
      "nd"
    } else if (day % 10 == 3 && day != 13) {
      "rd"
    } else {
      "th"
    }

  def formatDate(date: Date): String = {
    val dayFormat = new SimpleDateFormat("d")
    val restOfDateFormat = new SimpleDateFormat("MMMM yyyy 'at' hh:mma")

    val day = dayFormat.format(date).toInt
    val ordinalSuffix = getOrdinalSuffix(day)

    s"$day$ordinalSuffix ${restOfDateFormat.format(date)}"
  }
}
