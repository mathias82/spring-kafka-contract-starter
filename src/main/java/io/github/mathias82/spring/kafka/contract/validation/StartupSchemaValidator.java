package io.github.mathias82.spring.kafka.contract.validation;

import io.github.mathias82.spring.kafka.contract.autoconfigure.KafkaContractProperties;
import io.github.mathias82.spring.kafka.contract.exception.IncompatibleSchemaException;
import io.github.mathias82.spring.kafka.contract.exception.MissingSchemaException;
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
        for (SchemaSubject subject : properties.getSubjects()) {

            if (!client.subjectExists(subject.getName())) {
                throw new MissingSchemaException(
                        "Schema subject not found: " + subject.getName()
                );
            }

            if (client.getCompatibility(subject.getName()) != properties.getCompatibility()) {
                throw new IncompatibleSchemaException(
                        "Compatibility mismatch for subject: " + subject.getName()
                );
            }

            String schema = loadSchema(subject.getSchemaFile());

            if (!client.isCompatible(subject.getName(), schema)) {
                throw new IncompatibleSchemaException(
                        "Schema is NOT compatible for subject: " + subject.getName()
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
