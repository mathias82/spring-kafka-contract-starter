package io.github.mathias82.spring.kafka.contract.validation;

import io.github.mathias82.spring.kafka.contract.autoconfigure.KafkaContractProperties;
import io.github.mathias82.spring.kafka.contract.exception.IncompatibleSchemaException;
import io.github.mathias82.spring.kafka.contract.exception.MissingSchemaException;
import io.github.mathias82.spring.kafka.contract.model.CompatibilityMode;
import io.github.mathias82.spring.kafka.contract.model.SchemaSubject;
import io.github.mathias82.spring.kafka.contract.registry.SchemaRegistryClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

/**
 * Validates Kafka schema contracts at application startup.
 * Fails fast if required schemas are missing or incompatible.
 */
public class StartupSchemaValidator implements ApplicationRunner {

    private final KafkaContractProperties properties;
    private final SchemaRegistryClient client;

    public StartupSchemaValidator(KafkaContractProperties properties,
            SchemaRegistryClient client) {
        this.properties = properties;
        this.client = client;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        CompatibilityMode expectedCompatibility =
                properties.getCompatibility();

        for (SchemaSubject subject : properties.getSubjects()) {

            String subjectName = subject.getName();

            if (!client.subjectExists(subjectName)) {
                throw new MissingSchemaException(
                        "Schema subject not found: " + subjectName
                );
            }

            CompatibilityMode actualCompatibility =
                    client.getCompatibility(subjectName, expectedCompatibility);

            if (actualCompatibility != expectedCompatibility) {
                throw new IncompatibleSchemaException(
                        "Compatibility mismatch for subject '%s'. Expected=%s, Actual=%s"
                                .formatted(subjectName, expectedCompatibility, actualCompatibility)
                );
            }

            String schema = loadSchema(subject.getSchemaFile());

            if (!client.isCompatible(subjectName, schema)) {
                throw new IncompatibleSchemaException(
                        "Schema is NOT compatible for subject: " + subjectName
                );
            }
        }
    }

    private String loadSchema(Resource resource) throws Exception {
        return StreamUtils.copyToString(
                resource.getInputStream(),
                StandardCharsets.UTF_8
        );
    }
}
