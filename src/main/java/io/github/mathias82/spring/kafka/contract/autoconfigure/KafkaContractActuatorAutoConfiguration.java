package io.github.mathias82.spring.kafka.contract.autoconfigure;

import io.github.mathias82.spring.kafka.contract.actuator.KafkaContractEndpoint;
import io.github.mathias82.spring.kafka.contract.validation.ContractValidationReport;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = KafkaContractAutoConfiguration.class)
@ConditionalOnClass(Endpoint.class)
@ConditionalOnProperty(prefix = "kafka.contract", name = "enabled", havingValue = "true")
public class KafkaContractActuatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(KafkaContractEndpoint.class)
    KafkaContractEndpoint kafkaContractEndpoint(
            KafkaContractProperties properties,
            ContractValidationReport report) {
        return new KafkaContractEndpoint(properties, report);
    }
}
