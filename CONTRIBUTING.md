# Contributing to Spring Kafka Contract Starter

Thanks for considering a contribution.

## Ways to contribute

- Report bugs and unexpected Schema Registry behavior.
- Suggest focused features or compatibility improvements.
- Improve documentation and examples.
- Submit pull requests with tests for behavior changes.

## Development setup

### Requirements

- Java 21+
- Maven 3.9+

Docker is not required for the starter's unit test suite. The separate `spring-kafka-contract-demo` repository provides the real Kafka + Schema Registry end-to-end scenarios.

### Build and test

```bash
mvn clean verify
```

The GitHub Actions workflows also verify the supported Spring Boot compatibility matrix.

## Pull requests

Keep changes focused and describe:

- what changed and why
- how the change was tested
- whether it affects configuration, public APIs, or runtime behavior

For runtime changes, add or update tests. For configuration properties, keep Spring Boot configuration metadata documentation up to date.

## Issues and support

Use GitHub Issues for reproducible bugs, feature proposals, and compatibility problems. Include the Spring Boot version, starter version, Schema Registry environment, relevant configuration with secrets removed, and the smallest useful reproduction when possible.

## Code of Conduct

Participation in this project is governed by [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
