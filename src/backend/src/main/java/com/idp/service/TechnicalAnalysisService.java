package com.idp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class TechnicalAnalysisService {

  /**
   * Calculate Simple Moving Average (SMA).
   * Returns null for periods before the SMA period is reached.
   */
  public static List<BigDecimal> calculateSMA(List<BigDecimal> closes, int period) {
    List<BigDecimal> sma = new ArrayList<>();
    for (int i = 0; i < closes.size(); i++) {
      if (i < period - 1) {
        sma.add(null);
      } else {
        BigDecimal sum = BigDecimal.ZERO;
        for (int j = i - period + 1; j <= i; j++) {
          sum = sum.add(closes.get(j));
        }
        BigDecimal avg = sum.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
        sma.add(avg);
      }
    }
    return sma;
  }

  /**
   * Calculate Bollinger Bands (20-period SMA ± 2 standard deviations).
   */
  public static List<BollingerBand> calculateBollinger(List<BigDecimal> closes, int period) {
    List<BigDecimal> sma = calculateSMA(closes, period);
    List<BollingerBand> bands = new ArrayList<>();

    for (int i = 0; i < closes.size(); i++) {
      if (sma.get(i) == null) {
        bands.add(new BollingerBand(null, null, null));
      } else {
        BigDecimal mean = sma.get(i);
        BigDecimal variance = BigDecimal.ZERO;
        for (int j = i - period + 1; j <= i; j++) {
          BigDecimal diff = closes.get(j).subtract(mean);
          variance = variance.add(diff.multiply(diff));
        }
        variance = variance.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
        BigDecimal std = sqrt(variance);
        BigDecimal upper = mean.add(std.multiply(BigDecimal.valueOf(2))).setScale(2, RoundingMode.HALF_UP);
        BigDecimal lower = mean.subtract(std.multiply(BigDecimal.valueOf(2))).setScale(2, RoundingMode.HALF_UP);
        bands.add(new BollingerBand(upper, mean, lower));
      }
    }
    return bands;
  }

  /**
   * Calculate Relative Strength Index (RSI).
   */
  public static List<BigDecimal> calculateRSI(List<BigDecimal> closes, int period) {
    if (closes.size() < period + 1) {
      return closes.stream().map(v -> null).toList();
    }

    List<BigDecimal> rsi = new ArrayList<>();
    for (int i = 0; i < period; i++) {
      rsi.add(null);
    }

    BigDecimal[] gains = new BigDecimal[period];
    BigDecimal[] losses = new BigDecimal[period];

    for (int i = 1; i <= period; i++) {
      BigDecimal d = closes.get(i).subtract(closes.get(i - 1));
      gains[i - 1] = d.compareTo(BigDecimal.ZERO) > 0 ? d : BigDecimal.ZERO;
      losses[i - 1] = d.compareTo(BigDecimal.ZERO) < 0 ? d.negate() : BigDecimal.ZERO;
    }

    BigDecimal avgGain = sum(gains).divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
    BigDecimal avgLoss = sum(losses).divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);

    BigDecimal rsiValue = calculateRSIValue(avgGain, avgLoss);
    rsi.add(rsiValue);

    for (int i = period + 1; i < closes.size(); i++) {
      BigDecimal d = closes.get(i).subtract(closes.get(i - 1));
      BigDecimal gain = d.compareTo(BigDecimal.ZERO) > 0 ? d : BigDecimal.ZERO;
      BigDecimal loss = d.compareTo(BigDecimal.ZERO) < 0 ? d.negate() : BigDecimal.ZERO;

      avgGain = avgGain.multiply(BigDecimal.valueOf(period - 1))
          .add(gain)
          .divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
      avgLoss = avgLoss.multiply(BigDecimal.valueOf(period - 1))
          .add(loss)
          .divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);

      rsiValue = calculateRSIValue(avgGain, avgLoss);
      rsi.add(rsiValue);
    }

    return rsi;
  }

  private static BigDecimal calculateRSIValue(BigDecimal avgGain, BigDecimal avgLoss) {
    if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.valueOf(100);
    }
    BigDecimal rs = avgGain.divide(avgLoss, 8, RoundingMode.HALF_UP);
    return BigDecimal.valueOf(100)
        .subtract(BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), 2, RoundingMode.HALF_UP));
  }

  /**
   * Calculate Exponential Moving Average (EMA).
   */
  public static List<BigDecimal> calculateEMA(List<BigDecimal> closes, int period) {
    if (closes.size() < period) {
      return closes.stream().map(v -> null).toList();
    }

    List<BigDecimal> ema = new ArrayList<>();
    for (int i = 0; i < period - 1; i++) {
      ema.add(null);
    }

    // Seed EMA with SMA of first 'period' values
    BigDecimal seed = BigDecimal.ZERO;
    for (int i = 0; i < period; i++) {
      seed = seed.add(closes.get(i));
    }
    seed = seed.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
    ema.add(seed);

    BigDecimal k = BigDecimal.valueOf(2).divide(BigDecimal.valueOf(period + 1), 8, RoundingMode.HALF_UP);
    for (int i = period; i < closes.size(); i++) {
      BigDecimal prevEMA = ema.get(ema.size() - 1);
      BigDecimal newEMA = prevEMA.multiply(BigDecimal.ONE.subtract(k))
          .add(closes.get(i).multiply(k))
          .setScale(4, RoundingMode.HALF_UP);
      ema.add(newEMA);
    }

    return ema;
  }

  /**
   * Calculate MACD (Moving Average Convergence Divergence).
   */
  public static MACDResult calculateMACD(List<BigDecimal> closes, int fast, int slow, int signal) {
    List<BigDecimal> ema12 = calculateEMA(closes, fast);
    List<BigDecimal> ema26 = calculateEMA(closes, slow);

    List<BigDecimal> macdLine = new ArrayList<>();
    for (int i = 0; i < closes.size(); i++) {
      if (ema12.get(i) != null && ema26.get(i) != null) {
        BigDecimal macd = ema12.get(i).subtract(ema26.get(i)).setScale(4, RoundingMode.HALF_UP);
        macdLine.add(macd);
      } else {
        macdLine.add(null);
      }
    }

    List<BigDecimal> nonNullVals = new ArrayList<>();
    List<Integer> nonNullIdx = new ArrayList<>();
    for (int i = 0; i < macdLine.size(); i++) {
      if (macdLine.get(i) != null) {
        nonNullVals.add(macdLine.get(i));
        nonNullIdx.add(i);
      }
    }

    List<BigDecimal> sigEma = calculateEMA(nonNullVals, signal);
    List<BigDecimal> signalLine = new ArrayList<>();
    for (int i = 0; i < closes.size(); i++) {
      signalLine.add(null);
    }

    for (int j = 0; j < nonNullIdx.size(); j++) {
      int origIdx = nonNullIdx.get(j);
      if (j < sigEma.size() && sigEma.get(j) != null) {
        signalLine.set(origIdx, sigEma.get(j));
      }
    }

    List<BigDecimal> histogram = new ArrayList<>();
    for (int i = 0; i < closes.size(); i++) {
      if (macdLine.get(i) != null && signalLine.get(i) != null) {
        BigDecimal hist = macdLine.get(i).subtract(signalLine.get(i)).setScale(4, RoundingMode.HALF_UP);
        histogram.add(hist);
      } else {
        histogram.add(null);
      }
    }

    return new MACDResult(macdLine, signalLine, histogram, ema12, ema26);
  }

  /**
   * Calculate Stochastic Oscillator (%K and %D).
   */
  public static StochasticResult calculateStochastic(List<BigDecimal> highs, List<BigDecimal> lows,
      List<BigDecimal> closes, int k, int d) {
    List<BigDecimal> pctK = new ArrayList<>();

    for (int i = 0; i < closes.size(); i++) {
      if (i < k - 1) {
        pctK.add(null);
      } else {
        BigDecimal hh = highs.stream().skip(i - k + 1).limit(k).max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        BigDecimal ll = lows.stream().skip(i - k + 1).limit(k).min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        if (hh.equals(ll)) {
          pctK.add(BigDecimal.valueOf(50));
        } else {
          BigDecimal k_value = closes.get(i).subtract(ll).divide(hh.subtract(ll), 4, RoundingMode.HALF_UP)
              .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
          pctK.add(k_value);
        }
      }
    }

    List<BigDecimal> pctD = new ArrayList<>();
    for (int i = 0; i < closes.size(); i++) {
      if (pctK.get(i) == null) {
        pctD.add(null);
      } else {
        List<BigDecimal> vals = new ArrayList<>();
        for (int j = i; j >= 0 && vals.size() < d; j--) {
          if (pctK.get(j) != null) {
            vals.add(pctK.get(j));
          }
        }
        if (vals.size() < d) {
          pctD.add(null);
        } else {
          BigDecimal d_value = vals.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
              .divide(BigDecimal.valueOf(d), 2, RoundingMode.HALF_UP);
          pctD.add(d_value);
        }
      }
    }

    return new StochasticResult(pctK, pctD);
  }

  /**
   * Calculate On-Balance Volume (OBV).
   */
  public static List<Long> calculateOBV(List<BigDecimal> closes, List<Long> volumes) {
    List<Long> obv = new ArrayList<>();
    if (closes.isEmpty()) {
      return obv;
    }

    obv.add(0L);
    for (int i = 1; i < closes.size(); i++) {
      long vol = volumes.get(i) != null ? volumes.get(i) : 0;
      if (closes.get(i).compareTo(closes.get(i - 1)) > 0) {
        obv.add(obv.get(i - 1) + vol);
      } else if (closes.get(i).compareTo(closes.get(i - 1)) < 0) {
        obv.add(obv.get(i - 1) - vol);
      } else {
        obv.add(obv.get(i - 1));
      }
    }
    return obv;
  }

  /**
   * Calculate Money Flow Index (MFI).
   */
  public static List<BigDecimal> calculateMFI(List<BigDecimal> highs, List<BigDecimal> lows,
      List<BigDecimal> closes, List<Long> volumes, int period) {
    if (closes.size() < period + 1) {
      return closes.stream().map(v -> null).toList();
    }

    List<BigDecimal> typicalPrices = new ArrayList<>();
    for (int i = 0; i < closes.size(); i++) {
      BigDecimal tp = highs.get(i).add(lows.get(i)).add(closes.get(i))
          .divide(BigDecimal.valueOf(3), 4, RoundingMode.HALF_UP);
      typicalPrices.add(tp);
    }

    List<BigDecimal> mfi = new ArrayList<>();
    for (int i = 0; i < period; i++) {
      mfi.add(null);
    }

    BigDecimal posMF = BigDecimal.ZERO;
    BigDecimal negMF = BigDecimal.ZERO;

    for (int i = 1; i <= period; i++) {
      BigDecimal mf = typicalPrices.get(i)
          .multiply(BigDecimal.valueOf(volumes.get(i) != null ? volumes.get(i) : 0));
      if (typicalPrices.get(i).compareTo(typicalPrices.get(i - 1)) >= 0) {
        posMF = posMF.add(mf);
      } else {
        negMF = negMF.add(mf);
      }
    }

    mfi.add(calculateMFIValue(posMF, negMF));

    for (int i = period + 1; i < closes.size(); i++) {
      BigDecimal oldMF = typicalPrices.get(i - period)
          .multiply(BigDecimal.valueOf(volumes.get(i - period) != null ? volumes.get(i - period) : 0));
      if (typicalPrices.get(i - period).compareTo(typicalPrices.get(i - period - 1)) >= 0) {
        posMF = posMF.subtract(oldMF);
      } else {
        negMF = negMF.subtract(oldMF);
      }

      BigDecimal newMF = typicalPrices.get(i)
          .multiply(BigDecimal.valueOf(volumes.get(i) != null ? volumes.get(i) : 0));
      if (typicalPrices.get(i).compareTo(typicalPrices.get(i - 1)) >= 0) {
        posMF = posMF.add(newMF);
      } else {
        negMF = negMF.add(newMF);
      }

      mfi.add(calculateMFIValue(posMF, negMF));
    }

    return mfi;
  }

  private static BigDecimal calculateMFIValue(BigDecimal posMF, BigDecimal negMF) {
    if (negMF.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.valueOf(100);
    }
    BigDecimal mfi = BigDecimal.valueOf(100)
        .subtract(BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(posMF.divide(negMF, 8, RoundingMode.HALF_UP)), 2, RoundingMode.HALF_UP));
    return mfi;
  }

  // Helper methods
  private static BigDecimal sum(BigDecimal[] values) {
    BigDecimal result = BigDecimal.ZERO;
    for (BigDecimal v : values) {
      if (v != null) {
        result = result.add(v);
      }
    }
    return result;
  }

  private static BigDecimal sqrt(BigDecimal value) {
    if (value.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal x = value;
    BigDecimal y = BigDecimal.ONE;
    while (x.subtract(y).abs().compareTo(BigDecimal.valueOf(0.00000001)) > 0) {
      x = x.add(y).divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP);
      y = value.divide(x, 8, RoundingMode.HALF_UP);
    }
    return x;
  }

  // Result DTOs
  public static class BollingerBand {
    public final BigDecimal upper;
    public final BigDecimal middle;
    public final BigDecimal lower;

    public BollingerBand(BigDecimal upper, BigDecimal middle, BigDecimal lower) {
      this.upper = upper;
      this.middle = middle;
      this.lower = lower;
    }
  }

  public static class MACDResult {
    public final List<BigDecimal> macdLine;
    public final List<BigDecimal> signalLine;
    public final List<BigDecimal> histogram;
    public final List<BigDecimal> ema12;
    public final List<BigDecimal> ema26;

    public MACDResult(List<BigDecimal> macdLine, List<BigDecimal> signalLine, List<BigDecimal> histogram,
        List<BigDecimal> ema12, List<BigDecimal> ema26) {
      this.macdLine = macdLine;
      this.signalLine = signalLine;
      this.histogram = histogram;
      this.ema12 = ema12;
      this.ema26 = ema26;
    }
  }

  public static class StochasticResult {
    public final List<BigDecimal> pctK;
    public final List<BigDecimal> pctD;

    public StochasticResult(List<BigDecimal> pctK, List<BigDecimal> pctD) {
      this.pctK = pctK;
      this.pctD = pctD;
    }
  }
}
