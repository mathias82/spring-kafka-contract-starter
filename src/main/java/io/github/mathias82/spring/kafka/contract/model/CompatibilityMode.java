package io.github.mathias82.spring.kafka.contract.model;

/**
 * Defines the compatibility mode enforced against the Schema Registry.
 */
public enum CompatibilityMode {
    BACKWARD,
    FORWARD,
    FULL
}
