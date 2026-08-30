# 🚦 Spring Kafka Contract Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mathias82.spring.kafka/spring-kafka-contract-starter.svg)](https://central.sonatype.com/artifact/io.github.mathias82.spring.kafka/spring-kafka-contract-starter)
[![Website](https://img.shields.io/badge/Website-GitHub%20Pages-black)](https://mathias82.github.io/spring-kafka-contract-demo/)
[![Build](https://github.com/mathias82/spring-kafka-contract-starter/actions/workflows/build.yml/badge.svg)](https://github.com/mathias82/spring-kafka-contract-starter/actions)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen)

**Fail-fast Kafka Schema Registry contract enforcement for Spring Boot applications.**

The starter validates configured Kafka schema contracts during application startup. If a required subject is missing, the effective compatibility mode differs from the expected mode, or the local schema is incompatible with the latest registered version, startup fails.

## What it validates

- required Confluent Schema Registry subjects exist
- subject-level or global compatibility matches the configured expectation
- local schemas are compatible with the latest registered schema
- schema type is sent explicitly (`AVRO`, `JSON`, or `PROTOBUF`)
- registry communication failures are surfaced with a dedicated contract exception

Validation happens at startup, so there is no per-message runtime validation overhead.

## Installation

```xml
<dependency>
  <groupId>io.github.mathias82.spring.kafka</groupId>
  <artifactId>spring-kafka-contract-starter</artifactId>
  <version>0.2.0</version>
</dependency>
```

## Configuration

```yaml
kafka:
  contract:
    enabled: true
    compatibility: BACKWARD
    registry:
      url: http://localhost:8081
      connect-timeout-ms: 2000
      read-timeout-ms: 5000
      # Optional for Confluent Cloud / secured registries
      username: ${SCHEMA_REGISTRY_API_KEY:}
      password: ${SCHEMA_REGISTRY_API_SECRET:}
    subjects:
      - name: order-events-value
        schema-file: classpath:schemas/order-event.avsc
        schema-type: AVRO
```

`schema-type` defaults to `AVRO`.

Supported compatibility modes are `NONE`, `BACKWARD`, `BACKWARD_TRANSITIVE`, `FORWARD`, `FORWARD_TRANSITIVE`, `FULL`, and `FULL_TRANSITIVE`.

## Startup flow

For every configured subject the starter:

1. checks that the subject has a latest registered version
2. resolves compatibility using subject config, then global config, then the configured fallback
3. compares the effective mode with `kafka.contract.compatibility`
4. submits the local schema to the compatibility endpoint
5. fails startup when the contract is missing, incompatible, or the registry cannot be reached

Typical failures are surfaced as `MissingSchemaException`, `IncompatibleSchemaException`, or `SchemaRegistryCommunicationException`.

## Spring Boot integration

The project is a Spring Boot 3 auto-configuration registered through:

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

Consumers do not need to place their application under the `io.github.mathias82` package or manually import the configuration.

## Authentication and customization

When `username` is configured, the starter adds HTTP Basic authentication to its dedicated `kafkaContractRestTemplate`. This maps directly to Confluent Cloud Schema Registry API key/secret authentication. Applications can override the `SchemaRegistryClient` bean for custom integrations.

Subject names are URL-encoded before calls are made, and connect/read timeouts are configurable.

## Demo

The runnable companion project is available at https://github.com/mathias82/spring-kafka-contract-demo. It includes Kafka, Confluent Schema Registry, real producer → Kafka → consumer verification, and compatible/incompatible schema evolution examples.

## Scope

The built-in registry client currently targets the Confluent Schema Registry API. This project is a startup governance guardrail; it is not a Kafka client abstraction, registry replacement, serialization framework, or runtime message validator.

## Contributing

Contributions, issues, and discussions are welcome.
