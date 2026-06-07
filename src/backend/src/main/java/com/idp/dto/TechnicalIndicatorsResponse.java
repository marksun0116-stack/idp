package com.idp.dto;

import java.math.BigDecimal;
import java.util.List;

public record TechnicalIndicatorsResponse(
    List<BigDecimal> closes,
    List<Long> volumes,
    List<BigDecimal> sma20,
    List<BigDecimal> sma50,
    List<BollingerBandData> bollinger,
    List<BigDecimal> rsi,
    List<BigDecimal> macdLine,
    List<BigDecimal> signalLine,
    List<BigDecimal> histogram,
    List<BigDecimal> ema12,
    List<BigDecimal> ema26,
    List<BigDecimal> pctK,
    List<BigDecimal> pctD,
    List<Long> obv,
    List<BigDecimal> mfi
) {
  public record BollingerBandData(
      BigDecimal upper,
      BigDecimal middle,
      BigDecimal lower
  ) {}
}
