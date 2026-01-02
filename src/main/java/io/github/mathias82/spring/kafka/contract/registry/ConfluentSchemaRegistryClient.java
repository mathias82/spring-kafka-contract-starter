package io.github.mathias82.spring.kafka.contract.registry;

import io.github.mathias82.spring.kafka.contract.model.CompatibilityMode;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class ConfluentSchemaRegistryClient implements SchemaRegistryClient {

    private final String baseUrl;
    private final RestTemplate rest;

    public ConfluentSchemaRegistryClient(String baseUrl, RestTemplate rest) {
        this.baseUrl = baseUrl;
        this.rest = rest;
    }

    @Override
    public boolean subjectExists(String subject) {
        List<?> subjects = rest.getForObject(
                baseUrl + "/subjects",
                List.class
        );
        return subjects != null && subjects.contains(subject);
    }

    @Override
    public CompatibilityMode getCompatibility(String subject) {
        Map<?, ?> response = rest.getForObject(
                baseUrl + "/config/" + subject,
                Map.class
        );
        return CompatibilityMode.valueOf(
                response.get("compatibilityLevel").toString()
        );
    }

    @Override
    public boolean isCompatible(String subject, String schema) {
        Map<String, Object> request = Map.of("schema", schema);
        Map<?, ?> response = rest.postForObject(
                baseUrl + "/compatibility/subjects/" + subject + "/versions/latest",
                request,
                Map.class
        );
        return Boolean.TRUE.equals(response.get("is_compatible"));
    }
}
