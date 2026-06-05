package com.idp.dto;

import java.util.List;

public record DqsDriverResponse(
    String label,
    int impact,
    List<Long> relatedDecisionIds
) {
}
