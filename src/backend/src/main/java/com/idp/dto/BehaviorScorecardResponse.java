package com.idp.dto;

import java.util.List;

public record BehaviorScorecardResponse(
    int behavioralScore,
    int fomoScore,
    int lossAversionScore,
    int researchDisciplineScore,
    int riskDisciplineScore,
    List<BehaviorInsightResponse> insights
) {
}
