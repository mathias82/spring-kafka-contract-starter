# 🚦 SPRING KAFKA CONTRACT STARTER

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mathias82.spring.kafka/spring-kafka-contract-starter.svg)](https://repo1.maven.org/maven2/io/github/mathias82/spring/kafka/spring-kafka-contract-starter/0.1.0/)
[![Website](https://img.shields.io/badge/Website-GitHub%20Pages-black)](https://mathias82.github.io/spring-kafka-contract-demo/)
[![Build](https://github.com/mathias82/spring-kafka-contract-starter/actions/workflows/build.yml/badge.svg)](https://github.com/mathias82/spring-kafka-contract-starter/actions)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-black)
![Status](https://img.shields.io/badge/status-stable-brightgreen)

**Fail-fast Kafka schema contract enforcement for Spring Boot applications.**

Enforce Kafka data contracts **at application startup** by validating Schema Registry subjects, compatibility rules, and schema evolution — **before broken events reach production**.

---

## ❌ THE PROBLEM

Kafka **does not enforce data contracts**.

Even when using Schema Registry, applications can still:

- start with **missing schema subjects**
- ignore **compatibility rules**
- deploy **breaking schema changes**
- silently break **downstream consumers**

In **multi-team, event-driven architectures**, this leads to:

- late failures
- production incidents
- broken pipelines
- loss of trust in Kafka events

Schema Registry **stores schemas**, but it **does not protect you at application startup**.

---

## ✅ THE SOLUTION

**Spring Kafka Contract Starter** enforces Kafka schema contracts **before your application starts**.

It acts as a **governance guardrail**, not another Kafka abstraction.

At startup, it validates that:

- required Schema Registry subjects **exist**
- compatibility rules are **enforced** (`BACKWARD`, `FORWARD`, `FULL`)
- schemas are **compatible** with the registry
- invalid deployments **fail fast**

👉 **If anything is wrong → the application does not start.**

---

## 🚀 KEY FEATURES

- 🚦 **Fail-fast startup validation**
- 🔐 **Schema Registry compatibility enforcement**
- 🔁 **Safe schema evolution checks**
- ⚙️ **Spring Boot auto-configuration**
- 🧩 **Vendor-neutral design**
- 🪶 **Zero runtime overhead**
- 📦 **No Kafka abstraction layer**

---

## 📌 WHAT THIS IS (AND ISN’T)

### ✅ THIS IS:
- A **Kafka governance guardrail**
- A **startup safety net**
- A **schema contract enforcement mechanism**

### ❌ THIS IS NOT:
- A Kafka client abstraction
- A Schema Registry replacement
- A serialization framework
- A runtime message validator

---

## 🛠 INSTALLATION

```xml
<dependency>
  <groupId>io.github.mathias82.spring.kafka</groupId>
  <artifactId>spring-kafka-contract-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

📦 Available on Maven Central
👉 https://repo1.maven.org/maven2/io/github/mathias82/spring/kafka/spring-kafka-contract-starter/0.1.0/

⚙️ Configuration

kafka:
  contract:
    enabled: true
    compatibility: BACKWARD
    registry:
      type: confluent
      url: http://localhost:8081
    subjects:
      - name: order-events-value
        schema-file: classpath:schemas/order-event.avsc

🔍 What Happens at Startup

For each configured subject:
✔️ Check if the subject exists in Schema Registry
✔️ Resolve subject-level or global compatibility
✔️ Validate the local schema against registry rules
❌ Fail startup if anything is invalid

MissingSchemaException
IncompatibleSchemaException

No more silent contract violations.

🔁 Schema Evolution Safety

Supports safe evolution strategies:

- BACKWARD
- FORWARD
- FULL

Prevents:
- removing required fields
- changing field types
- incompatible schema change


🧪 Demo Project

A complete working demo is available:

👉 Spring Kafka Contract Demo
https://github.com/mathias82/spring-kafka-contract-demo

Includes:

- Docker Compose (Kafka + Schema Registry)
- Producer & Consumer REST APIs
- Schema evolution examples (v1 / v2 / v3)
- Postman collections
- Live walkthrough steps

🎯 Why This Matters

In real Kafka systems:

- contracts are architecture
- breaking events are production outages
- validation must be automatic, not tribal knowledge

This starter ensures:
- If the contract is broken, the app does not start.

🧠 Inspired By

1. Contract-first APIs
2. Database migration validation
3. Fail-fast system design
4. Real production Kafka failures

⭐ When Should You Use This?

Use it if:

- you run Kafka in production
- multiple teams publish events
- you care about schema evolution
- you want confidence in deployments

🙌 Contributing

Contributions, ideas, and discussions are welcome.
If this project saves you from a production incident, leave a ⭐.

