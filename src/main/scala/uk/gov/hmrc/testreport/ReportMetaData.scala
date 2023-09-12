package uk.gov.hmrc.testreport

import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.EncoderOps

case class ReportMetaData(
                           projectName: String,
                           jenkinsBuildId: Option[String],
                           jenkinsBuildUrl: String,
                           dateOfAssessment: String
                         )

object ReportMetaData {
  implicit val jsonEncoder: Encoder[ReportMetaData] = deriveEncoder[ReportMetaData]

  def getJsonString(reportMetaData: ReportMetaData): String = {
    reportMetaData.asJson.asString match {
      case Some(jsonString) => jsonString
      case _ => "";
    }
  }
}
