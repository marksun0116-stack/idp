package com.idp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ReviewType {
    THIRTY_DAYS("30d"),
    NINETY_DAYS("90d"),
    ONE_EIGHTY_DAYS("180d"),
    ONE_YEAR("1y");

    private final String value;

    ReviewType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ReviewType fromValue(String value) {
        return Arrays.stream(values())
            .filter(type -> type.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported reviewType: " + value));
    }
}
