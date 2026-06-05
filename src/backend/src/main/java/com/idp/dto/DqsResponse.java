package com.idp.dto;

import java.util.List;

public record DqsResponse(
    int score,
    int trend,
    DqsComponentsResponse components,
    List<DqsDriverResponse> drivers
) {
}
