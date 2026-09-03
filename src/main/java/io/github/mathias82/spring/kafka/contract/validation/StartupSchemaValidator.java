package io.github.mathias82.spring.kafka.contract.validation;

import io.github.mathias82.spring.kafka.contract.autoconfigure.KafkaContractProperties;
import io.github.mathias82.spring.kafka.contract.exception.IncompatibleSchemaException;
import io.github.mathias82.spring.kafka.contract.exception.MissingSchemaException;
import io.github.mathias82.spring.kafka.contract.exception.SchemaRegistryCommunicationException;
import io.github.mathias82.spring.kafka.contract.model.CompatibilityMode;
import io.github.mathias82.spring.kafka.contract.model.RegistryUnavailablePolicy;
import io.github.mathias82.spring.kafka.contract.model.SchemaSubject;
import io.github.mathias82.spring.kafka.contract.registry.SchemaRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Validates Kafka schema contracts at application startup.
 * Fails fast if required schemas are missing or incompatible.
 */
public class StartupSchemaValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupSchemaValidator.class);

    private final KafkaContractProperties properties;
    private final SchemaRegistryClient client;
    private final ContractValidationReport report;

    public StartupSchemaValidator(KafkaContractProperties properties,
            SchemaRegistryClient client,
            ContractValidationReport report) {
        this.properties = properties;
        this.client = client;
        this.report = report;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        CompatibilityMode expectedCompatibility = properties.getCompatibility();

        List<SchemaSubject> subjects = properties.getSubjects();
        for (int index = 0; index < subjects.size(); index++) {
            SchemaSubject subject = subjects.get(index);
            try {
                validateWithRetry(subject, expectedCompatibility);
            } catch (SchemaRegistryCommunicationException ex) {
                if (!ex.isRetryable()
                        || properties.getRegistry().getUnavailablePolicy() == RegistryUnavailablePolicy.FAIL) {
                    throw ex;
                }

                log.warn("Schema Registry remains unavailable after retries. Skipping startup contract validation "
                                + "because kafka.contract.registry.unavailable-policy=WARN. Cause: {}",
                        ex.getMessage());
                subjects.subList(index, subjects.size()).forEach(skipped ->
                        report.recordRegistryUnavailable(
                                skipped.getName(),
                                expectedCompatibility,
                                skipped.getSchemaType()));
                return;
            }
        }
    }

    private void validateWithRetry(SchemaSubject subject, CompatibilityMode expectedCompatibility) throws Exception {
        KafkaContractProperties.Retry retry = properties.getRetry();
        int maxAttempts = retry.getMaxAttempts();
        long backoffMs = retry.getInitialBackoffMs();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                validateSubject(subject, expectedCompatibility);
                return;
            } catch (SchemaRegistryCommunicationException ex) {
                if (!ex.isRetryable() || attempt == maxAttempts) {
                    throw ex;
                }

                log.warn("Schema Registry communication failed for subject '{}' (attempt {}/{}). Retrying in {} ms",
                        subject.getName(), attempt, maxAttempts, backoffMs);
                sleep(backoffMs);
                backoffMs = nextBackoff(backoffMs, retry);
            }
        }
    }

    private void validateSubject(SchemaSubject subject, CompatibilityMode expectedCompatibility) throws Exception {
        String subjectName = subject.getName();

        if (!client.subjectExists(subjectName)) {
            throw new MissingSchemaException("Schema subject not found: " + subjectName);
        }

        CompatibilityMode actualCompatibility = client.getCompatibility(subjectName, expectedCompatibility);

        if (actualCompatibility != expectedCompatibility) {
            throw new IncompatibleSchemaException(
                    "Compatibility mismatch for subject '%s'. Expected=%s, Actual=%s"
                            .formatted(subjectName, expectedCompatibility, actualCompatibility)
            );
        }

        String schema = loadSchema(subject.getSchemaFile());

        if (!client.isCompatible(subjectName, schema, subject.getSchemaType())) {
            throw new IncompatibleSchemaException("Schema is NOT compatible for subject: " + subjectName);
        }

        report.recordValid(
                subjectName,
                expectedCompatibility,
                actualCompatibility,
                subject.getSchemaType()
        );
    }

    private long nextBackoff(long currentBackoffMs, KafkaContractProperties.Retry retry) {
        long candidate = (long) Math.ceil(currentBackoffMs * retry.getMultiplier());
        return Math.min(candidate, retry.getMaxBackoffMs());
    }

    private void sleep(long backoffMs) {
        if (backoffMs <= 0) {
            return;
        }

        try {
            Thread.sleep(backoffMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while retrying Schema Registry validation", ex);
        }
    }

    private String loadSchema(Resource resource) throws Exception {
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}
