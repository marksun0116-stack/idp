package com.idp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum DecisionType {
    BUY("buy"),
    SELL("sell"),
    HOLD("hold"),
    WATCH("watch"),
    AVOID("avoid");

    private final String value;

    DecisionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DecisionType fromValue(String value) {
        return Arrays.stream(values())
            .filter(type -> type.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported decisionType: " + value));
    }
}

