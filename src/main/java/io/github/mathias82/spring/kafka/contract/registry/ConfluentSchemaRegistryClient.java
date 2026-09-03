package io.github.mathias82.spring.kafka.contract.registry;

import io.github.mathias82.spring.kafka.contract.exception.SchemaRegistryCommunicationException;
import io.github.mathias82.spring.kafka.contract.model.CompatibilityMode;
import io.github.mathias82.spring.kafka.contract.model.SchemaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ConfluentSchemaRegistryClient implements SchemaRegistryClient {

    private final String registryUrl;
    private final RestTemplate restTemplate;

    public ConfluentSchemaRegistryClient(String registryUrl, RestTemplate restTemplate) {
        this.registryUrl = registryUrl;
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean subjectExists(String subject) {
        URI uri = uri("subjects", subject, "versions", "latest");
        try {
            restTemplate.getForObject(uri, Map.class);
            return true;
        } catch (HttpClientErrorException.NotFound ignored) {
            return false;
        } catch (RestClientException ex) {
            throw communicationFailure("check subject '%s'".formatted(subject), ex);
        }
    }

    @Override
    public CompatibilityMode getCompatibility(String subject, CompatibilityMode fallback) {
        CompatibilityMode subjectMode = fetchCompatibility(uri("config", subject));
        if (subjectMode != null) {
            return subjectMode;
        }

        CompatibilityMode globalMode = fetchCompatibility(uri("config"));
        return globalMode != null ? globalMode : fallback;
    }

    private CompatibilityMode fetchCompatibility(URI uri) {
        try {
            Map<?, ?> response = restTemplate.getForObject(uri, Map.class);
            if (response == null) {
                return null;
            }

            Object value = response.get("compatibilityLevel");
            if (value == null) {
                value = response.get("compatibility");
            }
            if (value == null || value.toString().isBlank()) {
                return null;
            }
            return CompatibilityMode.valueOf(value.toString().toUpperCase());
        } catch (HttpClientErrorException.NotFound ignored) {
            return null;
        } catch (RestClientException ex) {
            throw communicationFailure("read compatibility configuration", ex);
        }
    }

    @Override
    public boolean isCompatible(String subject, String schema, SchemaType schemaType) {
        Map<String, Object> request = new HashMap<>();
        request.put("schema", schema);
        request.put("schemaType", schemaType.name());

        try {
            Map<?, ?> response = restTemplate.postForObject(
                    uri("compatibility", "subjects", subject, "versions", "latest"),
                    request,
                    Map.class
            );
            return response != null && Boolean.TRUE.equals(response.get("is_compatible"));
        } catch (RestClientException ex) {
            throw communicationFailure("validate compatibility for subject '%s'".formatted(subject), ex);
        }
    }

    private URI uri(String... segments) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(registryUrl);
        for (String segment : segments) {
            builder.pathSegment(segment);
        }
        return builder.build().encode().toUri();
    }

    private SchemaRegistryCommunicationException communicationFailure(String operation, RestClientException ex) {
        String detail = "";
        boolean retryable = true;
        if (ex instanceof RestClientResponseException responseException) {
            HttpStatus status = HttpStatus.resolve(responseException.getStatusCode().value());
            int statusCode = responseException.getStatusCode().value();
            retryable = statusCode == 408 || statusCode == 429 || responseException.getStatusCode().is5xxServerError();
            detail = " (HTTP %d%s)".formatted(
                    statusCode,
                    status == null ? "" : " " + status.getReasonPhrase()
            );
        }
        return new SchemaRegistryCommunicationException(
                "Failed to %s against Schema Registry at %s%s".formatted(operation, registryUrl, detail),
                ex,
                retryable
        );
    }
}
