package io.github.mathias82.spring.kafka.contract.registry;

import io.github.mathias82.spring.kafka.contract.model.CompatibilityMode;

public interface SchemaRegistryClient {

    boolean subjectExists(String subject);

    CompatibilityMode getCompatibility(String subject);

    boolean isCompatible(String subject, String schema);
}
