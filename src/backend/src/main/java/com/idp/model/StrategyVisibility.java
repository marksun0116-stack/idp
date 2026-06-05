package com.idp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum StrategyVisibility {
    PRIVATE("private"),
    PUBLIC("public");

    private final String value;

    StrategyVisibility(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static StrategyVisibility fromValue(String value) {
        return Arrays.stream(values())
            .filter(visibility -> visibility.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported strategy visibility: " + value));
    }
}
