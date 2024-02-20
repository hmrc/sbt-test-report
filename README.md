# sbt-test-report

An sbt plugin for generating accessibility test reports at HMRC.

## How to use the plugin

### Getting started
* Ensure your UI test project is using the latest version of [ui-test-runner](https://github.com/hmrc/ui-test-runner/releases)
* Add the [latest version of this plugin](https://github.com/hmrc/sbt-test-report/releases) to your `/project/plugins.sbt` file, eg.
```scala
    addSbtPlugin("uk.gov.hmrc" % "sbt-test-report" % "X.Y.Z")
```
* Update your `run-tests.sh` to run `testReport` *after* your UI tests, eg.
```bash
#!/bin/bash -e
 
BROWSER=${1:=chrome}
ENVIRONMENT=${2:=local}

sbt clean -Dbrowser="${BROWSER}" -Denvironment="${ENVIRONMENT}" "testOnly uk.gov.hmrc.ui.specs.*" testReport
```

### Excluding certain paths from the report
By default, all pages visited by your UI tests will be assessed, and any accessibility violations shown on the report.
However, your UI test journeys might also visit services that you don't own, eg. auth-login-stub,
or perhaps some test-only routes in your service, to set up some initial state for testing.

To filter out violations reported on such pages, you can add them to `accessibility-assessment.json` in the top-level
of your project.  This file contains a list of `exclusions`, each consisting of
* a `path` regex, to match on any part of a visited URL
* a `reason`, briefly explaining why you're filtering out that path

eg.
```json
{
  "exclusions": [
    {
      "path": "/auth-login-stub",
      "reason": "Stubbed auth endpoints are owned by a different team."
    },
    {
      "path": "/test-only/",
      "reason": "Test-only endpoints do not need accessibility testing as they are used purely for testing purposes."
    }
  ]
}
```

Note that the Digital Inclusion and Accessibility Standards team (DIAS) will review these rules as part of an
internal accessibility assessment, so they should only be used for legitimate purposes.
