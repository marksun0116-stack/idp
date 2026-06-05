package com.idp.dto;

import com.idp.model.SymbolVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddTrackedSymbolRequest(
    @NotBlank String symbol,
    String note,
    List<String> tags,
    @NotNull SymbolVisibility visibility
) {
}
