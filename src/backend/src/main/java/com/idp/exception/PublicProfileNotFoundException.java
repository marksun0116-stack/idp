package com.idp.exception;

public class PublicProfileNotFoundException extends RuntimeException {
    public PublicProfileNotFoundException() {
        super("Public profile not found");
    }
}
