package io.github.mathias82.spring.kafka.contract.autoconfigure;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaContractConfigurationMetadataTest {

    @Test
    void generatedMetadataDocumentsPublicConfigurationProperties() throws IOException {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("META-INF/spring-configuration-metadata.json")) {
            assertThat(input).as("Spring Boot configuration metadata").isNotNull();

            String metadata = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(metadata)
                    .contains("kafka.contract.enabled")
                    .contains("Whether startup contract validation is enabled.")
                    .contains("kafka.contract.registry.url")
                    .contains("Base URL of the Confluent-compatible Schema Registry.")
                    .contains("kafka.contract.retry.max-attempts")
                    .contains("Maximum number of attempts for transient Schema Registry communication failures.");
        }
    }
}
