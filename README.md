# sbt-test-report

A plugin for test reports at HMRC.

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

### Testing

> **Warning**
> before you push your changes to GitHub, please ensure that the running service is stopped. This is necessary to revert any file modifications that occur in the `data.js` file while the service is running for development purposes.


To run tests please start the service to host the static web page
```
npm start
```

Then run the tests
```
npm test
```

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
