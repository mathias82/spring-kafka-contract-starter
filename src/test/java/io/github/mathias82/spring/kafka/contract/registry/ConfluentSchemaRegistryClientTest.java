package io.github.mathias82.spring.kafka.contract.registry;

import io.github.mathias82.spring.kafka.contract.exception.SchemaRegistryCommunicationException;
import io.github.mathias82.spring.kafka.contract.model.CompatibilityMode;
import io.github.mathias82.spring.kafka.contract.model.SchemaType;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

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
    void sendsJsonSchemaTypeToCompatibilityEndpoint() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        String schema = "{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"string\"}}}";

        server.expect(requestTo("http://registry.test/compatibility/subjects/orders-json-value/versions/latest"))
                .andExpect(content().json("{\"schemaType\":\"JSON\",\"schema\":" + jsonString(schema) + "}"))
                .andRespond(withSuccess("{\"is_compatible\":true}", MediaType.APPLICATION_JSON));

        ConfluentSchemaRegistryClient client = new ConfluentSchemaRegistryClient("http://registry.test", restTemplate);

        assertEquals(true, client.isCompatible("orders-json-value", schema, SchemaType.JSON));
        server.verify();
    }

    @Test
    void sendsProtobufSchemaTypeToCompatibilityEndpoint() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        String schema = "syntax = \"proto3\"; message Order { string id = 1; }";

        server.expect(requestTo("http://registry.test/compatibility/subjects/orders-proto-value/versions/latest"))
                .andExpect(content().json("{\"schemaType\":\"PROTOBUF\",\"schema\":" + jsonString(schema) + "}"))
                .andRespond(withSuccess("{\"is_compatible\":true}", MediaType.APPLICATION_JSON));

        ConfluentSchemaRegistryClient client = new ConfluentSchemaRegistryClient("http://registry.test", restTemplate);

        assertEquals(true, client.isCompatible("orders-proto-value", schema, SchemaType.PROTOBUF));
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
        assertFalse(exception.isRetryable());
        server.verify();
    }

    @Test
    void marksServerErrorsAsRetryable() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://registry.test/subjects/orders-value/versions/latest"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        ConfluentSchemaRegistryClient client = new ConfluentSchemaRegistryClient("http://registry.test", restTemplate);

        SchemaRegistryCommunicationException exception = assertThrows(
                SchemaRegistryCommunicationException.class,
                () -> client.subjectExists("orders-value")
        );
        assertTrue(exception.isRetryable());
        server.verify();
    }

    private static String jsonString(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r") + "\"";
    }
}
