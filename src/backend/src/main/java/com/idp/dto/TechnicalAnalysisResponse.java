package com.idp.dto;

/**
 * Combined response containing both technical indicators and recommendation.
 */
public record TechnicalAnalysisResponse(
    TechnicalIndicatorsResponse indicators,
    TechnicalRecommendationResponse recommendation
) {}
