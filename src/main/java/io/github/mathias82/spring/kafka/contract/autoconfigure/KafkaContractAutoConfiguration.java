package io.github.mathias82.spring.kafka.contract.autoconfigure;

import io.github.mathias82.spring.kafka.contract.registry.ConfluentSchemaRegistryClient;
import io.github.mathias82.spring.kafka.contract.registry.SchemaRegistryClient;
import io.github.mathias82.spring.kafka.contract.validation.ContractValidationReport;
import io.github.mathias82.spring.kafka.contract.validation.StartupSchemaValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.util.StringUtils;
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

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        String username = properties.getRegistry().getUsername();
        if (StringUtils.hasText(username)) {
            restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(
                    username,
                    properties.getRegistry().getPassword() == null ? "" : properties.getRegistry().getPassword()
            ));
        }
        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(SchemaRegistryClient.class)
    SchemaRegistryClient schemaRegistryClient(
            KafkaContractProperties properties,
            @Qualifier("kafkaContractRestTemplate") RestTemplate restTemplate) {
        return new ConfluentSchemaRegistryClient(
                properties.getRegistry().getUrl(),
                restTemplate
        );
    }

    @Bean
    @ConditionalOnMissingBean(ContractValidationReport.class)
    ContractValidationReport contractValidationReport() {
        return new ContractValidationReport();
    }

    @Bean
    @ConditionalOnMissingBean(StartupSchemaValidator.class)
    StartupSchemaValidator startupSchemaValidator(
            KafkaContractProperties properties,
            SchemaRegistryClient client,
            ContractValidationReport report) {
        return new StartupSchemaValidator(properties, client, report);
    }
}
