package com.idp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Technical recommendation engine ported from stock-monitor.
 * Detects chart patterns, validates against historical similar setups,
 * and provides confidence-scored trading recommendations.
 */
public class TechnicalRecommendationEngine {

  private static final int SCORE_START_INDEX = 55;
  private static final int DEFAULT_HORIZON = 20;
  private static final int DEFAULT_MIN_SAMPLES = 5;

  /**
   * Build technical recommendation from indicator data.
   */
  public static TechnicalRecommendation buildRecommendation(IndicatorContext ctx) {
    int latestIdx = ctx.closes.size() - 1;
    BarScore current = scoreBar(ctx, latestIdx);

    if (current == null || current.items.isEmpty() || current.direction.equals("none")) {
      return emptyRecommendation("No Validated Edge",
          "Current setup does not have enough aligned trend inputs.", current);
    }

    if (current.direction.equals("mixed")) {
      return emptyRecommendation("Mixed Trend",
          "Bullish and bearish trend inputs are both present.", current);
    }

    // Find similar historical setups based on indicator value ranges (not signal conclusions)
    List<Double> samples = new ArrayList<>();
    List<SimilarSetup> similarSetups = new ArrayList<>();

    for (int i = SCORE_START_INDEX; i <= latestIdx - DEFAULT_HORIZON; i++) {
      if (!indicatorValuesMatch(ctx, latestIdx, i)) continue;

      BarScore prior = scoreBar(ctx, i);
      if (prior == null || !current.direction.equals(prior.direction)) continue;

      double forwardReturn = pct(ctx.closes.get(i + DEFAULT_HORIZON), ctx.closes.get(i));
      if (!Double.isFinite(forwardReturn)) continue;

      samples.add(forwardReturn);
      similarSetups.add(new SimilarSetup(
          i,
          ctx.closes.get(i),
          forwardReturn,
          prior.direction,
          prior.strategy.name
      ));
    }

    // Calculate confidence
    int wins = 0;
    for (double ret : samples) {
      if (current.direction.equals("bullish") && ret > 0) wins++;
      else if (current.direction.equals("bearish") && ret < 0) wins++;
    }

    double winRate = samples.isEmpty() ? 0 : (wins * 100.0 / samples.size());
    double medianReturn = median(samples);
    boolean supported = samples.size() >= DEFAULT_MIN_SAMPLES && winRate >= 55
        && ((current.direction.equals("bullish") && medianReturn > 0) ||
            (current.direction.equals("bearish") && medianReturn < 0));

    String confidence = !supported ? "Low"
        : samples.size() >= 20 && winRate >= 65 ? "High"
        : samples.size() >= 8 && winRate >= 58 ? "Medium"
        : "Low";

    // Determine direction and label
    String directionLabel;
    String finalDirection;

    if (supported) {
      // High/Medium confidence: use validated signal direction
      directionLabel = current.direction.equals("bullish") ? "Bullish" : "Bearish";
      finalDirection = current.direction;
    } else if (!samples.isEmpty()) {
      // Low confidence with data: infer direction from median return
      if (medianReturn > 0) {
        directionLabel = "Bullish";
        finalDirection = "bullish";
      } else if (medianReturn < 0) {
        directionLabel = "Bearish";
        finalDirection = "bearish";
      } else {
        directionLabel = "No Validated Edge";
        finalDirection = "none";
      }
    } else {
      // No data at all
      directionLabel = "No Validated Edge";
      finalDirection = "none";
    }

    String label = supported ? directionLabel
        : !samples.isEmpty() ? directionLabel + " (limited validation)"
        : directionLabel;

    return new TechnicalRecommendation(
        label,
        confidence,
        current.strategy.name,
        current.strategy.rationale,
        current.strategy.invalidation,
        samples.size(),
        winRate,
        median(samples),
        finalDirection,
        similarSetups,
        current.items
    );
  }

  private static BarScore scoreBar(IndicatorContext ctx, int idx) {
    if (idx < 0 || idx >= ctx.closes.size()) return null;

    String regime = classifyRegime(ctx, idx);
    Strategy strategy = detectStrategy(ctx, regime, idx);
    if (strategy == null) return null;

    List<Signal> items = new ArrayList<>();

    // Moving Average signals
    if (valid(ctx.sma20.get(idx)) && valid(ctx.sma50.get(idx))) {
      if (ctx.sma20.get(idx).compareTo(ctx.sma50.get(idx)) > 0) {
        items.add(new Signal("MA Alignment", "Buy", "SMA20 > SMA50"));
      } else {
        items.add(new Signal("MA Alignment", "Sell", "SMA20 < SMA50"));
      }
    }

    // Price position relative to moving averages
    if (valid(ctx.sma20.get(idx))) {
      BigDecimal price = ctx.closes.get(idx);
      if (price.compareTo(ctx.sma20.get(idx)) > 0) {
        items.add(new Signal("Price Position", "Buy", "Price > SMA20"));
      } else if (price.compareTo(ctx.sma20.get(idx)) < 0) {
        items.add(new Signal("Price Position", "Sell", "Price < SMA20"));
      }
    }

    // RSI signals - based on momentum and level
    if (valid(ctx.rsi.get(idx))) {
      BigDecimal rsiVal = ctx.rsi.get(idx);
      if (rsiVal.compareTo(BigDecimal.valueOf(30)) < 0) {
        items.add(new Signal("RSI", "Buy", "Oversold (< 30)"));
      } else if (rsiVal.compareTo(BigDecimal.valueOf(70)) > 0) {
        items.add(new Signal("RSI", "Sell", "Overbought (> 70)"));
      } else if (rsiVal.compareTo(BigDecimal.valueOf(50)) > 0) {
        items.add(new Signal("RSI", "Buy", "Bullish momentum (> 50)"));
      } else if (rsiVal.compareTo(BigDecimal.valueOf(50)) < 0) {
        items.add(new Signal("RSI", "Sell", "Bearish momentum (< 50)"));
      }
    }

    // MACD signals - based on histogram and trend
    if (valid(ctx.histogram.get(idx))) {
      BigDecimal histogram = ctx.histogram.get(idx);
      if (histogram.compareTo(BigDecimal.ZERO) > 0) {
        items.add(new Signal("MACD", "Buy", "Bullish (Histogram > 0)"));
      } else if (histogram.compareTo(BigDecimal.ZERO) < 0) {
        items.add(new Signal("MACD", "Sell", "Bearish (Histogram < 0)"));
      }
    }

    if (items.isEmpty()) return null;

    // Determine direction based on majority of signals
    long buys = items.stream().filter(s -> s.signal.equals("Buy")).count();
    long sells = items.stream().filter(s -> s.signal.equals("Sell")).count();

    String direction = buys > sells ? "bullish" : sells > buys ? "bearish" : "mixed";
    if (direction.equals("mixed")) return null;

    return new BarScore(regime, direction, strategy, items);
  }

  private static String classifyRegime(IndicatorContext ctx, int idx) {
    if (idx < 50) return "conflicted";

    BigDecimal close = ctx.closes.get(idx);
    boolean maTrendUp = valid(ctx.sma20.get(idx)) && valid(ctx.sma50.get(idx))
        && ctx.sma20.get(idx).compareTo(ctx.sma50.get(idx)) > 0
        && rising(ctx.sma20, idx) && rising(ctx.sma50, idx);

    boolean maTrendDown = valid(ctx.sma20.get(idx)) && valid(ctx.sma50.get(idx))
        && ctx.sma20.get(idx).compareTo(ctx.sma50.get(idx)) < 0
        && falling(ctx.sma20, idx) && falling(ctx.sma50, idx);

    if (maTrendUp && close.compareTo(ctx.sma20.get(idx)) > 0) return "trending-up";
    if (maTrendDown && close.compareTo(ctx.sma20.get(idx)) < 0) return "trending-down";

    return "conflicted";
  }

  private static Strategy detectStrategy(IndicatorContext ctx, String regime, int idx) {
    if (regime.equals("trending-up")) {
      return new Strategy("SMA Crossover (20/50)",
          "Price above 20-SMA, 20-SMA above 50-SMA, both rising",
          "Close below 50-SMA would invalidate");
    } else if (regime.equals("trending-down")) {
      return new Strategy("SMA Crossover (20/50)",
          "Price below 20-SMA, 20-SMA below 50-SMA, both falling",
          "Close above 50-SMA would invalidate");
    }
    return null;
  }

  private static boolean indicatorValuesMatch(IndicatorContext ctx, int currentIdx, int priorIdx) {
    if (!valid(ctx.rsi.get(currentIdx)) || !valid(ctx.rsi.get(priorIdx))) {
      return false; // Need RSI for matching
    }

    BigDecimal currentRsi = ctx.rsi.get(currentIdx);
    BigDecimal priorRsi = ctx.rsi.get(priorIdx);

    // RSI must be within 15 points (e.g., 40-50 range)
    if (Math.abs(currentRsi.doubleValue() - priorRsi.doubleValue()) > 15) {
      return false;
    }

    // SMA relationship: Check if SMA20/SMA50 difference is similar
    if (valid(ctx.sma20.get(currentIdx)) && valid(ctx.sma50.get(currentIdx)) &&
        valid(ctx.sma20.get(priorIdx)) && valid(ctx.sma50.get(priorIdx))) {

      // Calculate percentage difference between SMA20 and SMA50
      double currentSmaDiff = (ctx.sma20.get(currentIdx).doubleValue() / ctx.sma50.get(currentIdx).doubleValue() - 1) * 100;
      double priorSmaDiff = (ctx.sma20.get(priorIdx).doubleValue() / ctx.sma50.get(priorIdx).doubleValue() - 1) * 100;

      // Difference in SMA relationship should be within 3% (e.g., both 1-4% apart or -2-1% apart)
      if (Math.abs(currentSmaDiff - priorSmaDiff) > 3) {
        return false;
      }
    }

    // MACD histogram: Similar momentum direction and magnitude
    if (valid(ctx.histogram.get(currentIdx)) && valid(ctx.histogram.get(priorIdx))) {
      BigDecimal currentHist = ctx.histogram.get(currentIdx);
      BigDecimal priorHist = ctx.histogram.get(priorIdx);

      // Both should be on same side of zero (both positive or both negative)
      if ((currentHist.compareTo(BigDecimal.ZERO) > 0) != (priorHist.compareTo(BigDecimal.ZERO) > 0)) {
        return false;
      }

      // Magnitude should be within 50% (e.g., if current is 1.0, accept 0.5-1.5)
      double currentHistAbs = Math.abs(currentHist.doubleValue());
      double priorHistAbs = Math.abs(priorHist.doubleValue());
      if (currentHistAbs > 0 && priorHistAbs > 0) {
        double ratio = Math.max(currentHistAbs, priorHistAbs) / Math.min(currentHistAbs, priorHistAbs);
        if (ratio > 2.0) { // Allow up to 2x difference
          return false;
        }
      }
    }

    return true;
  }

  private static boolean valid(BigDecimal v) {
    return v != null && v.compareTo(BigDecimal.ZERO) != 0;
  }

  private static boolean rising(List<BigDecimal> arr, int idx) {
    if (idx < 5) return false;
    return valid(arr.get(idx)) && valid(arr.get(idx - 5)) && arr.get(idx).compareTo(arr.get(idx - 5)) > 0;
  }

  private static boolean falling(List<BigDecimal> arr, int idx) {
    if (idx < 5) return false;
    return valid(arr.get(idx)) && valid(arr.get(idx - 5)) && arr.get(idx).compareTo(arr.get(idx - 5)) < 0;
  }

  private static double pct(BigDecimal a, BigDecimal b) {
    if (a == null || b == null || b.compareTo(BigDecimal.ZERO) == 0) return Double.NaN;
    return a.subtract(b).divide(b, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
  }

  private static double median(List<Double> values) {
    if (values.isEmpty()) return 0;
    List<Double> sorted = new ArrayList<>(values);
    sorted.sort(Double::compareTo);
    int mid = sorted.size() / 2;
    return sorted.size() % 2 == 0 ? (sorted.get(mid - 1) + sorted.get(mid)) / 2 : sorted.get(mid);
  }

  private static TechnicalRecommendation emptyRecommendation(String label, String reason, BarScore current) {
    return new TechnicalRecommendation(
        label, "Low", "N/A", reason, "N/A", 0, 0, 0,
        current != null ? current.direction : "none",
        List.of()
    );
  }

  // Data classes
  public static class IndicatorContext {
    public List<BigDecimal> closes;
    public List<BigDecimal> sma20;
    public List<BigDecimal> sma50;
    public List<BigDecimal> rsi;
    public List<BigDecimal> histogram; // MACD histogram

    public IndicatorContext(List<BigDecimal> closes, List<BigDecimal> sma20, List<BigDecimal> sma50,
        List<BigDecimal> rsi, List<BigDecimal> histogram) {
      this.closes = closes;
      this.sma20 = sma20;
      this.sma50 = sma50;
      this.rsi = rsi;
      this.histogram = histogram;
    }
  }

  public static class BarScore {
    public String regime;
    public String direction;
    public Strategy strategy;
    public List<Signal> items;

    public BarScore(String regime, String direction, Strategy strategy, List<Signal> items) {
      this.regime = regime;
      this.direction = direction;
      this.strategy = strategy;
      this.items = items;
    }
  }

  public static class Strategy {
    public String name;
    public String rationale;
    public String invalidation;

    public Strategy(String name, String rationale, String invalidation) {
      this.name = name;
      this.rationale = rationale;
      this.invalidation = invalidation;
    }
  }

  public static class Signal {
    public String indicator;
    public String signal;
    public String detail;

    public Signal(String indicator, String signal, String detail) {
      this.indicator = indicator;
      this.signal = signal;
      this.detail = detail;
    }
  }

  public static class SimilarSetup {
    public int idx;
    public BigDecimal close;
    public double forwardReturn;
    public String direction;
    public String strategy;

    public SimilarSetup(int idx, BigDecimal close, double forwardReturn, String direction, String strategy) {
      this.idx = idx;
      this.close = close;
      this.forwardReturn = forwardReturn;
      this.direction = direction;
      this.strategy = strategy;
    }
  }

  public static class TechnicalRecommendation {
    public String label;
    public String confidence;
    public String strategy;
    public String reason;
    public String invalidation;
    public int sampleSize;
    public double winRate;
    public double medianReturn;
    public String direction;
    public List<SimilarSetup> similarSetups;
    public List<Signal> signals;

    public TechnicalRecommendation(String label, String confidence, String strategy, String reason,
        String invalidation, int sampleSize, double winRate, double medianReturn, String direction,
        List<SimilarSetup> similarSetups) {
      this(label, confidence, strategy, reason, invalidation, sampleSize, winRate, medianReturn, direction, similarSetups, List.of());
    }

    public TechnicalRecommendation(String label, String confidence, String strategy, String reason,
        String invalidation, int sampleSize, double winRate, double medianReturn, String direction,
        List<SimilarSetup> similarSetups, List<Signal> signals) {
      this.label = label;
      this.confidence = confidence;
      this.strategy = strategy;
      this.reason = reason;
      this.invalidation = invalidation;
      this.sampleSize = sampleSize;
      this.winRate = winRate;
      this.medianReturn = medianReturn;
      this.direction = direction;
      this.similarSetups = similarSetups;
      this.signals = signals;
    }
  }
}
