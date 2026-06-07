package com.idp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AccountType {
    BROKERAGE,
    IRA,
    ROTH_IRA,
    @JsonProperty("401K")
    FOUR_O_ONE_K,
    HSA
}
