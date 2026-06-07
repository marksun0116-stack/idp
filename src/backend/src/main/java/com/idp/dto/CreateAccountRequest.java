package com.idp.dto;

import com.idp.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
    @NotBlank String name,
    @NotNull AccountType accountType
) {
}
