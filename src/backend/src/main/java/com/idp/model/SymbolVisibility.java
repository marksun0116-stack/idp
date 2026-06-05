package com.idp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum SymbolVisibility {
    PRIVATE("private"),
    PUBLIC("public");

    private final String value;

    SymbolVisibility(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SymbolVisibility fromValue(String value) {
        return Arrays.stream(values())
            .filter(visibility -> visibility.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported symbol visibility: " + value));
    }
}
