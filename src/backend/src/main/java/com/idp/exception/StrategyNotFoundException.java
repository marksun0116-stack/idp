package com.idp.exception;

public class StrategyNotFoundException extends RuntimeException {
    public StrategyNotFoundException() {
        super("Strategy not found");
    }
}
