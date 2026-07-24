package org.example.exceptions;

public class SecondDataIsEarlierException extends RuntimeException {
    public SecondDataIsEarlierException(String message) {
        super(message);
    }
}
