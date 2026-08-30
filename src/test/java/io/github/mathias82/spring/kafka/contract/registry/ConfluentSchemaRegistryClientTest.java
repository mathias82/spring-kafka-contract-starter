package io.github.mathias82.spring.kafka.contract.registry;

import io.github.mathias82.spring.kafka.contract.exception.SchemaRegistryCommunicationException;
import io.github.mathias82.spring.kafka.contract.model.CompatibilityMode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

class ConfluentSchemaRegistryClientTest {

    @Test
    void readsConfluentCompatibilityLevel() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://registry.test/config/orders-value"))
                .andRespond(withSuccess("{\"compatibilityLevel\":\"FULL_TRANSITIVE\"}", MediaType.APPLICATION_JSON));

        ConfluentSchemaRegistryClient client = new ConfluentSchemaRegistryClient("http://registry.test", restTemplate);

        assertEquals(
                CompatibilityMode.FULL_TRANSITIVE,
                client.getCompatibility("orders-value", CompatibilityMode.BACKWARD)
        );
        server.verify();
    }

    @Test
    void fallsBackToGlobalCompatibilityWhenSubjectHasNoOverride() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://registry.test/config/orders-value"))
                .andRespond(withResourceNotFound());
        server.expect(requestTo("http://registry.test/config"))
                .andRespond(withSuccess("{\"compatibilityLevel\":\"BACKWARD_TRANSITIVE\"}", MediaType.APPLICATION_JSON));

        ConfluentSchemaRegistryClient client = new ConfluentSchemaRegistryClient("http://registry.test", restTemplate);

        assertEquals(
                CompatibilityMode.BACKWARD_TRANSITIVE,
                client.getCompatibility("orders-value", CompatibilityMode.BACKWARD)
        );
        server.verify();
    }

    @Test
    void urlEncodesSubjectNames() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://registry.test/subjects/orders%20value/versions/latest"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ConfluentSchemaRegistryClient client = new ConfluentSchemaRegistryClient("http://registry.test", restTemplate);

        assertEquals(true, client.subjectExists("orders value"));
        server.verify();
    }

    @Test
    void wrapsRegistryHttpFailuresWithActionableException() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://registry.test/subjects/orders-value/versions/latest"))
                .andRespond(withUnauthorizedRequest());

        ConfluentSchemaRegistryClient client = new ConfluentSchemaRegistryClient("http://registry.test", restTemplate);

        SchemaRegistryCommunicationException exception = assertThrows(
                SchemaRegistryCommunicationException.class,
                () -> client.subjectExists("orders-value")
        );
        assertEquals(true, exception.getMessage().contains("HTTP 401"));
        server.verify();
    }
}
