package com.idp.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MarketDataService {
    Optional<MarketQuote> quote(String symbol);

    List<MarketHistoryPoint> history(String symbol, String range);

    record MarketQuote(
        String symbol,
        BigDecimal lastPrice,
        BigDecimal change,
        BigDecimal percentChange,
        Long marketTime,
        String marketState,
        Long volume
    ) {
    }

    record MarketHistoryPoint(
        Long timestamp,
        BigDecimal close
    ) {
    }
}
