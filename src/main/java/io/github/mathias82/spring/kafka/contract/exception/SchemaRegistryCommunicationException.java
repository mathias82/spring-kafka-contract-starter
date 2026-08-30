package io.github.mathias82.spring.kafka.contract.exception;

public class SchemaRegistryCommunicationException extends ContractEnforcementException {

    public SchemaRegistryCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
