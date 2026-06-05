package com.idp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PublicProfileRequest(
    @NotBlank @Size(max = 40) String handle,
    @NotBlank @Size(max = 120) String displayName,
    @Size(max = 1000) String bio,
    List<String> publishedMetricIds
) {
}
