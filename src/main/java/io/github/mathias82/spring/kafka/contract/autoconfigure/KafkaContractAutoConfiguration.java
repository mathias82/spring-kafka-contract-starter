package io.github.mathias82.spring.kafka.contract.autoconfigure;

import io.github.mathias82.spring.kafka.contract.registry.ConfluentSchemaRegistryClient;
import io.github.mathias82.spring.kafka.contract.registry.SchemaRegistryClient;
import io.github.mathias82.spring.kafka.contract.validation.StartupSchemaValidator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(KafkaContractProperties.class)
@ConditionalOnProperty(prefix = "kafka.contract", name = "enabled", havingValue = "true")
public class KafkaContractAutoConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SchemaRegistryClient schemaRegistryClient(
            KafkaContractProperties properties,
            RestTemplate restTemplate) {

        if (properties.getRegistry().getType()
                == KafkaContractProperties.RegistryType.CONFLUENT) {
            return new ConfluentSchemaRegistryClient(
                    properties.getRegistry().getUrl(),
                    restTemplate
            );
        }

        throw new IllegalStateException("Unsupported registry type");
    }

    @Bean
    public StartupSchemaValidator startupSchemaValidator(
            KafkaContractProperties properties,
            SchemaRegistryClient client) {
        return new StartupSchemaValidator(properties, client);
    }
}
