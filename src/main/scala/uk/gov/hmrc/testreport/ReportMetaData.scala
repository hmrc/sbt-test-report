package uk.gov.hmrc.testreport

import io.circe.*
import io.circe.generic.semiauto.*

case class ReportMetaData(
  projectName: String,
  jenkinsBuildId: Option[String],
  jenkinsBuildUrl: String,
  dateOfAssessment: String
)

object ReportMetaData {
  implicit val jsonEncoder: Encoder[ReportMetaData] = deriveEncoder[ReportMetaData]
}
