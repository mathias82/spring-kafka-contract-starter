package io.github.mathias82.spring.kafka.contract.actuator;

import io.github.mathias82.spring.kafka.contract.autoconfigure.KafkaContractProperties;
import io.github.mathias82.spring.kafka.contract.validation.ContractValidationReport;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.LinkedHashMap;
import java.util.Map;

@Endpoint(id = "kafkaContracts")
public class KafkaContractEndpoint {

    private final KafkaContractProperties properties;
    private final ContractValidationReport report;

    public KafkaContractEndpoint(KafkaContractProperties properties, ContractValidationReport report) {
        this.properties = properties;
        this.report = report;
    }

    @ReadOperation
    public Map<String, Object> contracts() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("enabled", properties.isEnabled());
        response.put("expectedCompatibility", properties.getCompatibility());
        response.put("registryUrl", properties.getRegistry().getUrl());
        response.put("subjects", report.getSubjects());
        return response;
    }
}
