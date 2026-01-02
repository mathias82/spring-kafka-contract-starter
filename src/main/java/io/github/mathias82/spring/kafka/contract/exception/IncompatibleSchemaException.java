package io.github.mathias82.spring.kafka.contract.exception;

public class IncompatibleSchemaException extends RuntimeException {
    public IncompatibleSchemaException(String message) {
        super(message);
    }
}
