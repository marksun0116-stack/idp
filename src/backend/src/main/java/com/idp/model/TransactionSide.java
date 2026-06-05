package com.idp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum TransactionSide {
    BUY("buy"),
    SELL("sell");

    private final String value;

    TransactionSide(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TransactionSide fromValue(String value) {
        return Arrays.stream(values())
            .filter(side -> side.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported transaction side: " + value));
    }
}
