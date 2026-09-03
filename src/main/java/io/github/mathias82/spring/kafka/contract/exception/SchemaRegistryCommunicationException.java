package io.github.mathias82.spring.kafka.contract.exception;

public class SchemaRegistryCommunicationException extends ContractEnforcementException {

    private final boolean retryable;

    public SchemaRegistryCommunicationException(String message, Throwable cause) {
        this(message, cause, true);
    }

    public SchemaRegistryCommunicationException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
