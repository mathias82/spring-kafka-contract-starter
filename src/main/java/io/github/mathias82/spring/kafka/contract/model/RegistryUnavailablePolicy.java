package io.github.mathias82.spring.kafka.contract.model;

/**
 * Controls startup behavior when Schema Registry remains temporarily unavailable
 * after the configured retry attempts are exhausted.
 */
public enum RegistryUnavailablePolicy {
    /** Fail application startup. */
    FAIL,

    /** Log a warning, record validation as skipped, and continue startup. */
    WARN
}
