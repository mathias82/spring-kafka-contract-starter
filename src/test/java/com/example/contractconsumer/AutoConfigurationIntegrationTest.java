package com.example.contractconsumer;

import io.github.mathias82.spring.kafka.contract.autoconfigure.KafkaContractAutoConfiguration;
import io.github.mathias82.spring.kafka.contract.registry.SchemaRegistryClient;
import io.github.mathias82.spring.kafka.contract.validation.StartupSchemaValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = AutoConfigurationIntegrationTest.TestApplication.class,
        properties = {
                "kafka.contract.enabled=true",
                "kafka.contract.registry.url=http://localhost:8081"
        }
)
class AutoConfigurationIntegrationTest {

    @Autowired
    ApplicationContext context;

    @Test
    void starterAutoConfigurationLoadsOutsideLibraryPackage() {
        assertThat(context.getBean(KafkaContractAutoConfiguration.class)).isNotNull();
        assertThat(context.getBean(SchemaRegistryClient.class)).isNotNull();
        assertThat(context.getBean(StartupSchemaValidator.class)).isNotNull();
        assertThat(context.containsBean("kafkaContractRestTemplate")).isTrue();
    }

    @SpringBootApplication
    static class TestApplication {
    }
}
