package com.idp.dto;

import java.time.Instant;

public record PublicProfileUpdateResponse(
    String handle,
    Instant updatedAt
) {
}
