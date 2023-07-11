package uk.gov.hmrc.testreport

import sbt.*
import sbt.Keys.target
import scalatags.Text.all.*

object TestReportPlugin extends AutoPlugin {

  object autoImport {
    val testReport      = taskKey[Unit]("generate test report")
    val outputDirectory = settingKey[File]("output directory")
    val outputFile      = settingKey[File]("output file")
  }

  import autoImport.*

  override lazy val projectSettings: Seq[Def.Setting[?]] = Seq(
    testReport := generateTestReport().value,
    outputDirectory := (target.value / "test-reports" / "accessibility-assessment"),
    outputFile := (outputDirectory.value / "index.html")
  )

  private def generateTestReport(): Def.Initialize[Task[Unit]] = Def.task {
    os.makeDir.all(os.Path(outputDirectory.value / "assets"))

    val assets = List(
      "hmrc-frontend-5.28.0.min.css",
      "jquery.1.13.4.dataTables.min.css",
      "jquery.1.13.4.dataTables.min.js",
      "jquery.3.7.0.min.js"
    )

    assets.foreach { fileName =>
      os.write.over(
        os.Path(outputDirectory.value / "assets" / fileName),
        os.read(os.resource(getClass.getClassLoader) / "assets" / fileName)
      )
    }

    val axeResultsDirectory = os.Path(outputDirectory.value / "axe-results")
    val axeResults          = os.list.stream(axeResultsDirectory).filter(os.isDir).map { timestampDirectory =>
      ujson.read(os.read(timestampDirectory / "axeResults.json"))
    }

    os.write.over(
      os.Path(outputFile.value),
      html(
        head(
          link(rel := "stylesheet", href := "assets/hmrc-frontend-5.28.0.min.css"),
          link(rel := "stylesheet", href := "assets/jquery.1.13.4.dataTables.min.css"),
          script(src := "assets/jquery.3.7.0.min.js"),
          script(src := "assets/jquery.1.13.4.dataTables.min.js")
        ),
        body(
          h1(cls := "govuk-heading-xl", "HMRC test report"),
          table(
            cls := "govuk-table axe-results-table display",
            width := "100%",
            thead(
              tr(
                th("URL"),
                th("ID"),
                th("Description"),
                th("HTML"),
                th("Impact")
              )
            ),
            tbody(
              axeResults.map { result =>
                val violations = result("violations").arr
                violations.map { violation =>
                  tr(
                    td(result("url").str),
                    td(a(href := violation("helpUrl").str, violation("id").str)),
                    td(violation("description").str),
                    td(
                      pre(violation("nodes").arr.map(instance => pre(whiteSpace := "pre-wrap", instance("html").str)))
                    ),
                    td(violation("impact").str)
                  )
                }
              }
            )
          ),
          script("var table = new DataTable('.axe-results-table', {pageLength: 100});")
        )
      )
    )
  }

}
