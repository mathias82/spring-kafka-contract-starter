package io.github.mathias82.spring.kafka.contract.validation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(
        classes = StartupSchemaValidatorIT.TestApp.class,
        properties = {
                "kafka.contract.enabled=true",
                "kafka.contract.compatibility=BACKWARD",
                "kafka.contract.registry.type=confluent"
        }
)
class StartupSchemaValidatorIT {

    @SpringBootConfiguration
    static class TestApp {
        // empty on purpose
    }

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(
                    DockerImageName
                            .parse("confluentinc/cp-kafka:7.5.0")
                            .asCompatibleSubstituteFor("apache/kafka")
            );

    @Container
    static GenericContainer schemaRegistry =
            new GenericContainer(
                    DockerImageName.parse("confluentinc/cp-schema-registry:7.5.0")
            )
                    .withExposedPorts(8081)
                    .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                    .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
                    .withEnv(
                            "SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS",
                            "PLAINTEXT://" + kafka.getHost() + ":" + kafka.getFirstMappedPort()
                    );

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add(
                "kafka.contract.registry.url",
                () -> "http://localhost:" + schemaRegistry.getMappedPort(8081)
        );
    }

    @Test
    void applicationFailsIfSchemaMissing() {
    }
}
