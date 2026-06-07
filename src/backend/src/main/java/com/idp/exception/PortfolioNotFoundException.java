package com.idp.exception;

public class PortfolioNotFoundException extends RuntimeException {
    public PortfolioNotFoundException() {
        super("Portfolio resource not found");
    }
}
