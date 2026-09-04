# 🚦 Spring Kafka Contract Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mathias82.spring.kafka/spring-kafka-contract-starter.svg)](https://central.sonatype.com/artifact/io.github.mathias82.spring.kafka/spring-kafka-contract-starter)
[![Release](https://img.shields.io/github/v/release/mathias82/spring-kafka-contract-starter)](https://github.com/mathias82/spring-kafka-contract-starter/releases/latest)
[![Build](https://github.com/mathias82/spring-kafka-contract-starter/actions/workflows/build.yml/badge.svg)](https://github.com/mathias82/spring-kafka-contract-starter/actions)
[![Demo](https://img.shields.io/badge/demo-runnable-success)](https://github.com/mathias82/spring-kafka-contract-demo)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3%20%7C%203.4-brightgreen)

**Catch incompatible Kafka schema changes before your Spring Boot application starts serving traffic.**

`spring-kafka-contract-starter` is a fail-fast startup guardrail for applications using Confluent Schema Registry. It verifies that required subjects exist, compatibility policy is what you expect, and your local schema is compatible with the latest registered version. A broken contract stops deployment early instead of becoming a production Kafka surprise.

## Quick start

Add the released starter from Maven Central:

```xml
<dependency>
  <groupId>io.github.mathias82.spring.kafka</groupId>
  <artifactId>spring-kafka-contract-starter</artifactId>
  <version>0.2.4</version>
</dependency>
```

Configure the registry and the contracts your application owns:

```yaml
kafka:
  contract:
    enabled: true
    compatibility: BACKWARD
    registry:
      url: http://localhost:8081
    subjects:
      - name: order-events-value
        schema-file: classpath:schemas/order-event.avsc
        schema-type: AVRO
```

If the subject is missing, the configured compatibility mode is unexpected, or the schema is incompatible, application startup fails with a contract-specific exception.

## Why use it?

Schema Registry protects schema evolution, but applications still need a reliable point at which to assert the contracts they expect. This starter turns those assumptions into an executable startup check.

- **Fail fast** before the application becomes ready.
- **No per-message validation overhead** — validation happens at startup.
- **Real Schema Registry compatibility checks**, not local-only schema comparison.
- **AVRO, JSON Schema, and Protobuf** compatibility requests.
- **All Confluent compatibility modes**: `NONE`, `BACKWARD`, `BACKWARD_TRANSITIVE`, `FORWARD`, `FORWARD_TRANSITIVE`, `FULL`, `FULL_TRANSITIVE`.
- **Confluent Cloud / secured registry support** through Basic Auth credentials.
- **Bounded retry with exponential backoff** for transient registry communication failures.
- **Configurable registry-outage policy** to fail startup or continue with an observable warning after retries.
- **Actuator visibility** for contract validation status when Spring Boot Actuator is present.
- **Overrideable `SchemaRegistryClient`** for custom integrations.

## Where it fits

This project complements build-time schema checks; it does not replace them.

Confluent's Schema Registry Maven and Gradle plugins are useful in developer and CI workflows for tasks such as schema registration, compatibility checks, and testing against a registry. This starter addresses a different enforcement point: **application startup in the environment where the application is actually being deployed**.

| Enforcement point | Typical purpose | This starter |
| --- | --- | --- |
| Developer / build | Catch schema problems before packaging | Complementary |
| CI/CD | Validate schemas before promotion | Complementary |
| Application startup | Assert the deployed app's expected subjects, compatibility policy, and schema against the target registry | **Primary focus** |
| Per-message runtime | Validate every Kafka message | Not in scope |

<p align="center">
  <img src="docs/architecture.svg" alt="Kafka contract enforcement lifecycle" width="100%">
</p>

The goal is defense in depth: build-time checks catch issues early, while startup validation catches drift between assumptions made during development and the registry state seen by the deployed application.

## What it validates

For every configured subject the starter:

1. checks that the subject has a latest registered version
2. resolves compatibility using subject config, then global config, then the configured fallback
3. compares the effective mode with `kafka.contract.compatibility`
4. submits the local schema to the Schema Registry compatibility endpoint
5. retries transient network failures, HTTP `408`, `429`, and `5xx` responses
6. applies the configured unavailable-registry policy only after retry exhaustion

Typical failures are surfaced as `MissingSchemaException`, `IncompatibleSchemaException`, or `SchemaRegistryCommunicationException`.

## Production configuration

```yaml
kafka:
  contract:
    enabled: true
    compatibility: BACKWARD
    registry:
      url: ${SCHEMA_REGISTRY_URL}
      connect-timeout-ms: 2000
      read-timeout-ms: 5000
      unavailable-policy: FAIL
      username: ${SCHEMA_REGISTRY_API_KEY:}
      password: ${SCHEMA_REGISTRY_API_SECRET:}
    retry:
      max-attempts: 3
      initial-backoff-ms: 500
      multiplier: 2.0
      max-backoff-ms: 5000
    subjects:
      - name: order-events-value
        schema-file: classpath:schemas/order-event.avsc
        schema-type: AVRO
```

`schema-type` defaults to `AVRO`. `unavailable-policy` defaults to `FAIL`, preserving fail-fast behavior. Set it to `WARN` to continue startup when the registry remains temporarily unavailable after retries; affected subjects are reported as `SKIPPED_REGISTRY_UNAVAILABLE` through Actuator. Missing subjects, incompatible contracts, authentication/authorization failures, and other non-retryable `4xx` responses always fail startup. Set `max-attempts: 1` to disable retries.

## Actuator

When Spring Boot Actuator is available, the starter exposes Kafka contract validation status through the `kafkaContracts` actuator endpoint. This makes the result of startup contract validation observable without putting validation on the message-processing path.

## Spring Boot integration

The project is Spring Boot 3 auto-configuration registered through:

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

The starter is continuously verified against Spring Boot 3.3 and 3.4 on Java 21. Consumers do not need to place their application under the `io.github.mathias82` package or manually import configuration.

## Authentication and customization

When `username` is configured, the starter adds HTTP Basic authentication to its dedicated `kafkaContractRestTemplate`. This maps directly to Confluent Cloud Schema Registry API key/secret authentication. Applications can override the `SchemaRegistryClient` bean for custom integrations.

Subject names are URL-encoded before calls are made, and connect/read timeouts are configurable.

## Runnable proof

The companion [spring-kafka-contract-demo](https://github.com/mathias82/spring-kafka-contract-demo) runs Kafka and Confluent Schema Registry and verifies the published Maven Central artifact end to end.

Its CI proves five scenarios:

1. baseline v1 starts and completes a real producer → Kafka → consumer round trip
2. backward-compatible v2 starts successfully
3. intentionally breaking v3 is rejected during startup
4. a temporary registry outage allows startup with the `WARN` policy
5. the same outage rejects startup with the `FAIL` policy

That means the demo exercises both the real Kafka runtime path and the fail-fast contract behavior.

## Scope

The built-in registry client currently targets the Confluent Schema Registry API. This project is a startup governance guardrail; it is not a Kafka client abstraction, registry replacement, serialization framework, or runtime message validator.

## Contributing

Issues, ideas, bug reports, and pull requests are welcome. If Kafka schema compatibility has caused a deployment or production problem in your environment, opening an issue with the use case is especially useful for shaping the project.
