package com.idp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ReviewStatus {
    PENDING("pending"),
    COMPLETED("completed"),
    DISMISSED("dismissed"),
    RESCHEDULED("rescheduled");

    private final String value;

    ReviewStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ReviewStatus fromValue(String value) {
        return Arrays.stream(values())
            .filter(status -> status.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported review status: " + value));
    }
}
