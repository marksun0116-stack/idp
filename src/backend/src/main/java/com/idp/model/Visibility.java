package com.idp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Visibility {
    PRIVATE("private");

    private final String value;

    Visibility(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Visibility fromValue(String value) {
        if (PRIVATE.value.equalsIgnoreCase(value)) {
            return PRIVATE;
        }
        throw new IllegalArgumentException("Phase 1 supports private visibility only");
    }
}

