package com.idp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Indicates the source of a decision record.
 * MANUAL: User manually entered decision (Investment section)
 * AUTO: System automatically captured decision with latest price (Strategy section)
 */
public enum DecisionSource {
    MANUAL("manual"),
    AUTO("auto");

    private final String value;

    DecisionSource(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DecisionSource fromValue(String value) {
        return Arrays.stream(values())
            .filter(source -> source.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported source: " + value));
    }
}
