echo "--- removing /Users/vivrichards/.ivy2/local/uk.gov.hmrc/sbt-test-report/"
rm -rf /Users/vivrichards/.ivy2/local/uk.gov.hmrc/sbt-test-report/

echo "--- switching to sbt-test-report plugin directory"
cd /Users/vivrichards/dev/hmrc/sbt-test-report/
echo "--- rebuilding plugin"
sbt publishLocal

echo "--- switching to /Users/vivrichards/dev/hmrc/platform-test-example-ui-journey-tests directory"
cd /Users/vivrichards/dev/hmrc/platform-test-example-ui-journey-tests
echo "--- reloading sbt plugin"
sbt compile

echo "--- running testReport task"
sbt testReport

echo "--- report regenerated"
echo "file:///Users/vivrichards/dev/hmrc/platform-test-example-ui-journey-tests/target/test-reports/accessibility-assessment/html-report/index.html"
echo "--- opening report "
open file:///Users/vivrichards/dev/hmrc/platform-test-example-ui-journey-tests/target/test-reports/accessibility-assessment/html-report/index.html