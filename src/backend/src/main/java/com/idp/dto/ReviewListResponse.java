package com.idp.dto;

import java.util.List;

public record ReviewListResponse(List<ReviewSummaryResponse> reviews) {
}
