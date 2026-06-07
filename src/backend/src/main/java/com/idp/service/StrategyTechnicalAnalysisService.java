package com.idp.service;

import com.idp.dto.TechnicalAnalysisResponse;
import com.idp.dto.TechnicalIndicatorsResponse;
import com.idp.dto.TechnicalIndicatorsResponse.BollingerBandData;
import com.idp.dto.TechnicalRecommendationResponse;
import com.idp.dto.TechnicalRecommendationResponse.SimilarSetupData;
import com.idp.service.MarketDataService.MarketHistoryPoint;
import com.idp.service.TechnicalAnalysisService.BollingerBand;
import com.idp.service.TechnicalAnalysisService.MACDResult;
import com.idp.service.TechnicalAnalysisService.StochasticResult;
import com.idp.service.TechnicalRecommendationEngine.IndicatorContext;
import com.idp.service.TechnicalRecommendationEngine.TechnicalRecommendation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class StrategyTechnicalAnalysisService {

  private final MarketDataService marketDataService;

  public StrategyTechnicalAnalysisService(MarketDataService marketDataService) {
    this.marketDataService = marketDataService;
  }

  /**
   * Calculate all technical indicators for a symbol within a given time range.
   * Range examples: "1d", "1w", "1mo", "3mo", "6mo", "1y", "2y", "3y", "4y", "5y"
   */
  public TechnicalIndicatorsResponse getIndicators(String symbol, String range) {
    // Fetch historical OHLCV data from market data service
    List<MarketHistoryPoint> history = marketDataService.history(symbol, range);

    if (history.isEmpty()) {
      // Return empty response if no data available
      return new TechnicalIndicatorsResponse(
          List.of(), List.of(), List.of(), List.of(), List.of(),
          List.of(), List.of(), List.of(), List.of(), List.of(),
          List.of(), List.of(), List.of(), List.of(), List.of()
      );
    }

    // Extract OHLCV data
    List<BigDecimal> closes = new ArrayList<>();
    List<BigDecimal> highs = new ArrayList<>();
    List<BigDecimal> lows = new ArrayList<>();
    List<Long> volumes = new ArrayList<>();

    for (MarketHistoryPoint h : history) {
      closes.add(h.close());
      highs.add(h.high() != null ? h.high() : h.close());
      lows.add(h.low() != null ? h.low() : h.close());
      volumes.add(h.volume() != null ? h.volume() : 0L);
    }

    // Calculate all indicators
    List<BigDecimal> sma20 = TechnicalAnalysisService.calculateSMA(closes, 20);
    List<BigDecimal> sma50 = TechnicalAnalysisService.calculateSMA(closes, 50);

    List<BollingerBand> bollingerBands = TechnicalAnalysisService.calculateBollinger(closes, 20);
    List<BollingerBandData> bollingerData = bollingerBands.stream()
        .map(b -> new BollingerBandData(b.upper, b.middle, b.lower))
        .toList();

    List<BigDecimal> rsi = TechnicalAnalysisService.calculateRSI(closes, 14);

    MACDResult macd = TechnicalAnalysisService.calculateMACD(closes, 12, 26, 9);

    StochasticResult stochastic = TechnicalAnalysisService.calculateStochastic(highs, lows, closes, 14, 3);

    List<Long> obv = TechnicalAnalysisService.calculateOBV(closes, volumes);

    List<BigDecimal> mfi = TechnicalAnalysisService.calculateMFI(highs, lows, closes, volumes, 14);

    return new TechnicalIndicatorsResponse(
        closes,
        volumes,
        sma20,
        sma50,
        bollingerData,
        rsi,
        macd.macdLine,
        macd.signalLine,
        macd.histogram,
        macd.ema12,
        macd.ema26,
        stochastic.pctK,
        stochastic.pctD,
        obv,
        mfi
    );
  }

  /**
   * Get complete technical analysis including indicators and recommendation.
   */
  public TechnicalAnalysisResponse getAnalysis(String symbol, String range) {
    TechnicalIndicatorsResponse indicators = getIndicators(symbol, range);

    if (indicators.closes().isEmpty()) {
      // Return empty response if no data
      return new TechnicalAnalysisResponse(indicators, toRecommendationResponse(
          TechnicalRecommendationEngine.buildRecommendation(
              new IndicatorContext(List.of(), List.of(), List.of(), List.of(), List.of())
          )
      ));
    }

    // Build recommendation engine context from indicators
    IndicatorContext ctx = new IndicatorContext(
        indicators.closes(),
        indicators.sma20(),
        indicators.sma50(),
        indicators.rsi(),
        indicators.histogram()
    );

    TechnicalRecommendation recommendation = TechnicalRecommendationEngine.buildRecommendation(ctx);
    TechnicalRecommendationResponse recResponse = toRecommendationResponse(recommendation);

    return new TechnicalAnalysisResponse(indicators, recResponse);
  }

  private TechnicalRecommendationResponse toRecommendationResponse(TechnicalRecommendation rec) {
    List<SimilarSetupData> setupData = rec.similarSetups.stream()
        .map(s -> new SimilarSetupData(s.idx, s.close, s.forwardReturn, s.direction, s.strategy))
        .toList();

    return new TechnicalRecommendationResponse(
        rec.label,
        rec.confidence,
        rec.strategy,
        rec.reason,
        rec.invalidation,
        rec.sampleSize,
        rec.winRate,
        rec.medianReturn,
        rec.direction,
        setupData
    );
  }
}
