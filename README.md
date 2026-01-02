# Spring Kafka Contract Starter

Fail-fast Kafka schema contract enforcement for Spring Boot applications.

## ❌ The Problem

Kafka does not enforce data contracts.
Schema Registry stores schemas, but applications can still:
- start with missing schemas
- ignore compatibility rules
- break downstream consumers silently

In multi-team systems, this leads to fragile pipelines and late failures.

## ✅ The Solution

This starter enforces Kafka data contracts at **application startup**.

It ensures that:
- required schema subjects exist
- compatibility rules are respected
- incompatible schemas fail fast
- governance is automated, not manual

## 🚀 Features

- Startup schema validation
- Compatibility enforcement (BACKWARD / FORWARD / FULL)
- Vendor-neutral Schema Registry support
- Zero boilerplate
- Spring Boot auto-configuration

## 🛠 Configuration

```yaml
kafka.contract:
  enabled: true
  compatibility: BACKWARD
  registry:
    type: confluent
    url: http://localhost:8081
  subjects:
    - name: orders-value
      schema-file: classpath:/avro/order.avsc

📌 What This Is (and Isn’t)

✅ Governance guardrail
❌ Not a Kafka abstraction
❌ Not a Schema Registry replacement

🎯 Why It Matters

Prevent broken Kafka data contracts before they reach production.

