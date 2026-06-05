package com.idp.dto;

import java.util.List;

public record DecisionListResponse(List<DecisionSummaryResponse> decisions) {
}
