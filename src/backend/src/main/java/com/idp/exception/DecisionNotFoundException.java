package com.idp.exception;

public class DecisionNotFoundException extends RuntimeException {
    public DecisionNotFoundException() {
        super("Decision not found");
    }
}
