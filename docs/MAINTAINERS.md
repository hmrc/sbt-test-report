## Development

### Scalafmt

Check all project files are formatted as expected as follows:

```bash
sbt scalafmtCheckAll scalafmtCheck
```

Format `*.sbt` and `project/*.scala` files as follows:

```bash
sbt scalafmtSbt
```

Format all project files as follows:

```bash
sbt scalafmtAll
```

### Tests

Run unit tests with
```bash
sbt test
```

Run scripted sbt tests with 
```bash
sbt publishLocal
sbt scripted
```
If you are running locally, update the plugins.sbt for each scripted test to use the local snapshot

If you get errors running locally due to missing sbt configuration, you can copy `sbt.boot.properties` from
[hmrc/build-jenkins-agent](https://github.com/hmrc/build-jenkins-agent/blob/main/sbt_files/sbt.boot.properties)
into your local `.sbt` directory.
