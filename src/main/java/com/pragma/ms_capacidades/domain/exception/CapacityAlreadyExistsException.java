package com.pragma.ms_capacidades.domain.exception;

public class CapacityAlreadyExistsException extends RuntimeException {
    public CapacityAlreadyExistsException(String message) {
        super(message);
    }
}
