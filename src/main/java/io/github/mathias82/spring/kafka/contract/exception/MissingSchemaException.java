package io.github.mathias82.spring.kafka.contract.exception;

public class MissingSchemaException extends RuntimeException {
    public MissingSchemaException(String message) {
        super(message);
    }
}
