package com.idp.dto;

import com.idp.model.AccountType;

public record CreateAccountResponse(
    Long id,
    String name,
    AccountType accountType
) {
}
