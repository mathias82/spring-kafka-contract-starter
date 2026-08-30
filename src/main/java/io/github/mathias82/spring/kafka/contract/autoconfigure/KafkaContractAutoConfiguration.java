package io.github.mathias82.spring.kafka.contract.autoconfigure;

import io.github.mathias82.spring.kafka.contract.registry.ConfluentSchemaRegistryClient;
import io.github.mathias82.spring.kafka.contract.registry.SchemaRegistryClient;
import io.github.mathias82.spring.kafka.contract.validation.StartupSchemaValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@EnableConfigurationProperties(KafkaContractProperties.class)
@ConditionalOnProperty(prefix = "kafka.contract", name = "enabled", havingValue = "true")
public class KafkaContractAutoConfiguration {

    @Bean(name = "kafkaContractRestTemplate")
    @ConditionalOnMissingBean(name = "kafkaContractRestTemplate")
    RestTemplate kafkaContractRestTemplate(KafkaContractProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getRegistry().getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getRegistry().getReadTimeoutMs());
        return new RestTemplate(requestFactory);
    }

    @Bean
    @ConditionalOnMissingBean(SchemaRegistryClient.class)
    SchemaRegistryClient schemaRegistryClient(
            KafkaContractProperties properties,
            @Qualifier("kafkaContractRestTemplate") RestTemplate restTemplate) {

        if (properties.getRegistry().getType()
                == KafkaContractProperties.RegistryType.CONFLUENT) {
            return new ConfluentSchemaRegistryClient(
                    properties.getRegistry().getUrl(),
                    restTemplate
            );
        }

        throw new IllegalStateException(
                "Unsupported Schema Registry type: " + properties.getRegistry().getType()
        );
    }

    @Bean
    @ConditionalOnMissingBean(StartupSchemaValidator.class)
    StartupSchemaValidator startupSchemaValidator(
            KafkaContractProperties properties,
            SchemaRegistryClient client) {
        return new StartupSchemaValidator(properties, client);
    }
}
