package com.industriousgnomes.tddrefactorkata.exceptions;

public class InvalidSourceException extends RuntimeException {
    public InvalidSourceException() {
    }

    public InvalidSourceException(String message) {
        super(message);
    }
}
