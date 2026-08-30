# 🚦 Spring Kafka Contract Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mathias82.spring.kafka/spring-kafka-contract-starter.svg)](https://repo1.maven.org/maven2/io/github/mathias82/spring/kafka/spring-kafka-contract-starter/0.1.0/)
[![Website](https://img.shields.io/badge/Website-GitHub%20Pages-black)](https://mathias82.github.io/spring-kafka-contract-demo/)
[![Build](https://github.com/mathias82/spring-kafka-contract-starter/actions/workflows/build.yml/badge.svg)](https://github.com/mathias82/spring-kafka-contract-starter/actions)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen)

**Fail-fast Kafka Schema Registry contract enforcement for Spring Boot applications.**

The starter validates configured Kafka schema contracts during application startup. If a required subject is missing, the effective compatibility mode is different from the expected mode, or the local schema is incompatible with the latest registered version, startup fails.

## What it validates

- required Schema Registry subjects exist
- subject-level or global compatibility matches the configured expectation
- local schemas are compatible with the latest registered schema
- schema type is sent explicitly to Confluent Schema Registry (`AVRO`, `JSON`, or `PROTOBUF`)

Validation happens at startup, so there is no per-message runtime validation overhead.

## Installation

```xml
<dependency>
  <groupId>io.github.mathias82.spring.kafka</groupId>
  <artifactId>spring-kafka-contract-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

The artifact is available from Maven Central.

## Configuration

```yaml
kafka:
  contract:
    enabled: true
    compatibility: BACKWARD
    registry:
      type: confluent
      url: http://localhost:8081
      connect-timeout-ms: 2000
      read-timeout-ms: 5000
    subjects:
      - name: order-events-value
        schema-file: classpath:schemas/order-event.avsc
        schema-type: AVRO
```

`schema-type` defaults to `AVRO` when omitted.

Supported schema type values:

- `AVRO`
- `JSON`
- `PROTOBUF`

## Startup flow

For every configured subject the starter:

1. checks that the subject has a latest registered version
2. resolves compatibility using subject config, then global config, then the configured fallback
3. compares the effective mode with `kafka.contract.compatibility`
4. submits the local schema to the Schema Registry compatibility endpoint
5. throws a startup exception when the contract is missing or incompatible

Typical failures are surfaced as `MissingSchemaException` or `IncompatibleSchemaException`.

## Spring Boot integration

The project is a Spring Boot 3 auto-configuration and is registered through:

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

Consumers do not need to place their application under the `io.github.mathias82` package or manually import the configuration.

## Customization

The starter creates an internal `kafkaContractRestTemplate` with configurable connect/read timeouts. Applications can override the `SchemaRegistryClient` bean to provide a custom registry integration.

## Demo

A runnable companion project is available at:

https://github.com/mathias82/spring-kafka-contract-demo

It includes Kafka, Confluent Schema Registry, schema initialization, compatible/incompatible evolution examples, producer/consumer endpoints, Postman examples, and a GitHub Pages walkthrough.

## Background

Design rationale and production motivation:

https://medium.com/@mstauroy/fail-fast-kafka-schema-contracts-in-spring-boot-before-production-breaks-1b080204b49e

## Scope

This project is a startup governance guardrail. It is not a Kafka client abstraction, Schema Registry replacement, serialization framework, or runtime message validator.

## Contributing

Contributions, issues, and discussions are welcome.
