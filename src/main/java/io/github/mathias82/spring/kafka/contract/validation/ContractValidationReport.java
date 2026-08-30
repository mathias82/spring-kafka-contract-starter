package io.github.mathias82.spring.kafka.contract.validation;

import io.github.mathias82.spring.kafka.contract.model.CompatibilityMode;
import io.github.mathias82.spring.kafka.contract.model.SchemaType;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory snapshot of the most recent startup contract validation results.
 */
public class ContractValidationReport {

    private final ConcurrentMap<String, SubjectStatus> subjects = new ConcurrentHashMap<>();

    public void recordValid(String subject,
            CompatibilityMode expectedCompatibility,
            CompatibilityMode actualCompatibility,
            SchemaType schemaType) {
        subjects.put(subject, new SubjectStatus(
                subject,
                "VALID",
                expectedCompatibility,
                actualCompatibility,
                schemaType
        ));
    }

    public List<SubjectStatus> getSubjects() {
        Collection<SubjectStatus> values = subjects.values();
        return values.stream()
                .sorted(java.util.Comparator.comparing(SubjectStatus::subject))
                .toList();
    }

    public record SubjectStatus(
            String subject,
            String status,
            CompatibilityMode expectedCompatibility,
            CompatibilityMode actualCompatibility,
            SchemaType schemaType) {
    }
}
