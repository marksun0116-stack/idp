package com.idp.controller;

import com.idp.dto.TechnicalIndicatorsResponse;
import com.idp.service.StrategyTechnicalAnalysisService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/strategies/{id}/analysis")
public class StrategyAnalysisController {

  private final StrategyTechnicalAnalysisService technicalAnalysisService;

  public StrategyAnalysisController(StrategyTechnicalAnalysisService technicalAnalysisService) {
    this.technicalAnalysisService = technicalAnalysisService;
  }

  /**
   * Get all technical indicators for a symbol in the strategy.
   * Range examples: "1d", "1w", "1mo", "3mo", "6mo", "1y", "2y", "3y", "4y", "5y"
   */
  @GetMapping("/{symbol}")
  public TechnicalIndicatorsResponse getSymbolAnalysis(
      @PathVariable("id") Long strategyId,
      @PathVariable("symbol") String symbol,
      @RequestParam(name = "range", defaultValue = "1y") String range,
      Authentication authentication
  ) {
    // TODO: Verify user owns the strategy
    return technicalAnalysisService.getIndicators(symbol, range);
  }
}
