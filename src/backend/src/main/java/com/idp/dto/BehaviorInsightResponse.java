package com.idp.dto;

import java.util.List;

public record BehaviorInsightResponse(
    String type,
    String title,
    String detail,
    List<Long> relatedDecisionIds
) {
}
