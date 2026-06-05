package com.idp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum DecisionStatus {
    DRAFT("draft"),
    ACTIVE("active"),
    CLOSED("closed"),
    ARCHIVED("archived");

    private final String value;

    DecisionStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DecisionStatus fromValue(String value) {
        return Arrays.stream(values())
            .filter(status -> status.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported status: " + value));
    }
}

