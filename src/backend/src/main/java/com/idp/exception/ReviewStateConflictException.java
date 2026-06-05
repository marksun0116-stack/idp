package com.idp.exception;

public class ReviewStateConflictException extends RuntimeException {
    public ReviewStateConflictException(String message) {
        super(message);
    }
}
