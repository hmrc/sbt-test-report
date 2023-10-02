/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.testreport

import java.text.SimpleDateFormat
import java.util.Date

object DataFormatter {
  private def getOrdinalSuffix(day: Int): String =
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
    val dayFormat        = new SimpleDateFormat("d")
    val restOfDateFormat = new SimpleDateFormat("MMMM yyyy 'at' h:mma")

    val day           = dayFormat.format(date).toInt
    val ordinalSuffix = getOrdinalSuffix(day)

    s"$day$ordinalSuffix ${restOfDateFormat.format(date)}"
  }
}
