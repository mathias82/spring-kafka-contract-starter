package io.github.mathias82.spring.kafka.contract.registry;

import io.github.mathias82.spring.kafka.contract.model.CompatibilityMode;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class ConfluentSchemaRegistryClient implements SchemaRegistryClient {

    private final String registryUrl;
    private final RestTemplate restTemplate;

    public ConfluentSchemaRegistryClient(String registryUrl,
            RestTemplate restTemplate) {
        this.registryUrl = registryUrl;
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean subjectExists(String subject) {
        List<?> subjects = restTemplate.getForObject(
                registryUrl + "/subjects",
                List.class
        );
        return subjects != null && subjects.contains(subject);
    }

    /**
     * Resolve compatibility mode for a subject.
     * Priority:
     * 1. Subject-level config
     * 2. Global registry config
     * 3. Fallback (application.yml)
     */
    @Override
    public CompatibilityMode getCompatibility(String subject, CompatibilityMode fallback) {

        CompatibilityMode subjectMode =
                fetchCompatibility(registryUrl + "/config/" + subject);

        if (subjectMode != null) {
            return subjectMode;
        }

        CompatibilityMode globalMode =
                fetchCompatibility(registryUrl + "/config");

        if (globalMode != null) {
            return globalMode;
        }

        return fallback;
    }

    private CompatibilityMode fetchCompatibility(String url) {
        try {
            CompatibilityResponse response =
                    restTemplate.getForObject(url, CompatibilityResponse.class);

            return response != null ? response.asModeOrNull() : null;

        } catch (HttpClientErrorException.NotFound ignored) {
            // compatibility not configured
            return null;
        }
    }

    @Override
    public boolean isCompatible(String subject, String schema) {
        Map<String, Object> request = Map.of("schema", schema);

        Map<?, ?> response = restTemplate.postForObject(
                registryUrl + "/compatibility/subjects/" + subject + "/versions/latest",
                request,
                Map.class
        );

        return response != null && Boolean.TRUE.equals(response.get("is_compatible"));
    }

    /**
     * Confluent Schema Registry response:
     * { "compatibility": "BACKWARD" }
     */
    record CompatibilityResponse(String compatibility) {

        CompatibilityMode asModeOrNull() {
            if (compatibility == null || compatibility.isBlank()) {
                return null;
            }
            return CompatibilityMode.valueOf(compatibility.toUpperCase());
        }
    }
}
