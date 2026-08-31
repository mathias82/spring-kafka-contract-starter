package io.github.mathias82.spring.kafka.contract.autoconfigure;

import io.github.mathias82.spring.kafka.contract.actuator.KafkaContractEndpoint;
import io.github.mathias82.spring.kafka.contract.registry.SchemaRegistryClient;
import io.github.mathias82.spring.kafka.contract.validation.ContractValidationReport;
import io.github.mathias82.spring.kafka.contract.validation.StartupSchemaValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class KafkaContractAutoConfigurationConditionsTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    KafkaContractAutoConfiguration.class,
                    KafkaContractActuatorAutoConfiguration.class));

    @Test
    void doesNotConfigureStarterWhenDisabled() {
        contextRunner
                .withPropertyValues("kafka.contract.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(SchemaRegistryClient.class);
                    assertThat(context).doesNotHaveBean(ContractValidationReport.class);
                    assertThat(context).doesNotHaveBean(StartupSchemaValidator.class);
                    assertThat(context).doesNotHaveBean(KafkaContractEndpoint.class);
                });
    }

    @Test
    void configuresCoreBeansWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "kafka.contract.enabled=true",
                        "kafka.contract.registry.url=http://localhost:8081")
                .run(context -> {
                    assertThat(context).hasSingleBean(SchemaRegistryClient.class);
                    assertThat(context).hasSingleBean(ContractValidationReport.class);
                    assertThat(context).hasSingleBean(StartupSchemaValidator.class);
                });
    }

    @Test
    void configuresActuatorEndpointWhenActuatorIsPresent() {
        contextRunner
                .withPropertyValues(
                        "kafka.contract.enabled=true",
                        "kafka.contract.registry.url=http://localhost:8081")
                .run(context -> assertThat(context).hasSingleBean(KafkaContractEndpoint.class));
    }

    @Test
    void backsOffWhenApplicationProvidesSchemaRegistryClient() {
        contextRunner
                .withUserConfiguration(CustomClientConfiguration.class)
                .withPropertyValues(
                        "kafka.contract.enabled=true",
                        "kafka.contract.registry.url=http://localhost:8081")
                .run(context -> {
                    assertThat(context).hasSingleBean(SchemaRegistryClient.class);
                    assertThat(context.getBean(SchemaRegistryClient.class))
                            .isSameAs(context.getBean("customSchemaRegistryClient"));
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomClientConfiguration {

        @Bean
        SchemaRegistryClient customSchemaRegistryClient() {
            return mock(SchemaRegistryClient.class);
        }
    }
}
