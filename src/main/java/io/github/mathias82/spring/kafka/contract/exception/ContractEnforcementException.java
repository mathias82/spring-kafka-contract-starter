package io.github.mathias82.spring.kafka.contract.exception;

public class ContractEnforcementException extends RuntimeException {
    public ContractEnforcementException(String message) {
        super(message);
    }
}
