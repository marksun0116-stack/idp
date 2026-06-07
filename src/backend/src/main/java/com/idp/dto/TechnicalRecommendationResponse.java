package com.idp.dto;

import java.math.BigDecimal;
import java.util.List;

public record TechnicalRecommendationResponse(
    String label,
    String confidence,
    String strategy,
    String reason,
    String invalidation,
    int sampleSize,
    double winRate,
    double medianReturn,
    String direction,
    List<SimilarSetupData> similarSetups
) {
  public record SimilarSetupData(
      int idx,
      BigDecimal close,
      double forwardReturn,
      String direction,
      String strategy
  ) {}
}
