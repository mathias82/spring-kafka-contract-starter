package io.github.mathias82.spring.kafka.contract.validation;

import io.github.mathias82.spring.kafka.contract.autoconfigure.KafkaContractProperties;
import io.github.mathias82.spring.kafka.contract.exception.IncompatibleSchemaException;
import io.github.mathias82.spring.kafka.contract.exception.MissingSchemaException;
import io.github.mathias82.spring.kafka.contract.exception.SchemaRegistryCommunicationException;
import io.github.mathias82.spring.kafka.contract.model.CompatibilityMode;
import io.github.mathias82.spring.kafka.contract.model.SchemaSubject;
import io.github.mathias82.spring.kafka.contract.model.SchemaType;
import io.github.mathias82.spring.kafka.contract.registry.SchemaRegistryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StartupSchemaValidatorTest {

    private KafkaContractProperties properties;
    private SchemaRegistryClient client;
    private StartupSchemaValidator validator;
    private SchemaSubject subject;

    @BeforeEach
    void setUp() {
        properties = new KafkaContractProperties();
        properties.setCompatibility(CompatibilityMode.BACKWARD);
        properties.getRetry().setInitialBackoffMs(0);

        subject = new SchemaSubject();
        subject.setName("orders-value");
        subject.setSchemaType(SchemaType.AVRO);
        subject.setSchemaFile(new ByteArrayResource(
                "{\"type\":\"record\",\"name\":\"Order\",\"fields\":[]}".getBytes(StandardCharsets.UTF_8)
        ));
        properties.setSubjects(List.of(subject));

        client = mock(SchemaRegistryClient.class);
        validator = new StartupSchemaValidator(properties, client);
    }

    @Test
    void failsFastWhenSubjectIsMissing() {
        when(client.subjectExists("orders-value")).thenReturn(false);
        assertThrows(MissingSchemaException.class, () -> validator.run(null));
        verify(client, times(1)).subjectExists("orders-value");
    }

    @Test
    void failsFastWhenCompatibilityModeDoesNotMatch() {
        when(client.subjectExists("orders-value")).thenReturn(true);
        when(client.getCompatibility("orders-value", CompatibilityMode.BACKWARD))
                .thenReturn(CompatibilityMode.FORWARD);

        assertThrows(IncompatibleSchemaException.class, () -> validator.run(null));
    }

    @Test
    void failsFastWhenLocalSchemaIsIncompatible() {
        when(client.subjectExists("orders-value")).thenReturn(true);
        when(client.getCompatibility("orders-value", CompatibilityMode.BACKWARD))
                .thenReturn(CompatibilityMode.BACKWARD);
        when(client.isCompatible(org.mockito.ArgumentMatchers.eq("orders-value"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(SchemaType.AVRO))).thenReturn(false);

        assertThrows(IncompatibleSchemaException.class, () -> validator.run(null));
    }

    @Test
    void succeedsWhenContractIsValid() {
        when(client.subjectExists("orders-value")).thenReturn(true);
        when(client.getCompatibility("orders-value", CompatibilityMode.BACKWARD))
                .thenReturn(CompatibilityMode.BACKWARD);
        when(client.isCompatible(org.mockito.ArgumentMatchers.eq("orders-value"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(SchemaType.AVRO))).thenReturn(true);

        assertDoesNotThrow(() -> validator.run(null));
    }

    @Test
    void retriesTransientRegistryFailuresAndThenSucceeds() {
        SchemaRegistryCommunicationException transientFailure =
                new SchemaRegistryCommunicationException("temporary registry failure", new RuntimeException("boom"));

        when(client.subjectExists("orders-value"))
                .thenThrow(transientFailure)
                .thenReturn(true);
        when(client.getCompatibility("orders-value", CompatibilityMode.BACKWARD))
                .thenReturn(CompatibilityMode.BACKWARD);
        when(client.isCompatible(org.mockito.ArgumentMatchers.eq("orders-value"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(SchemaType.AVRO))).thenReturn(true);

        assertDoesNotThrow(() -> validator.run(null));
        verify(client, times(2)).subjectExists("orders-value");
    }

    @Test
    void stopsAfterConfiguredRetryAttempts() {
        properties.getRetry().setMaxAttempts(2);
        SchemaRegistryCommunicationException failure =
                new SchemaRegistryCommunicationException("registry unavailable", new RuntimeException("boom"));
        when(client.subjectExists("orders-value")).thenThrow(failure);

        assertThrows(SchemaRegistryCommunicationException.class, () -> validator.run(null));
        verify(client, times(2)).subjectExists("orders-value");
    }
}
