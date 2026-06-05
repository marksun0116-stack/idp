package com.idp.service;

import com.idp.dto.AddTrackedSymbolRequest;
import com.idp.dto.AppendTransactionRequest;
import com.idp.dto.CreateStrategyRequest;
import com.idp.dto.StrategyHistoryPointResponse;
import com.idp.dto.StrategyHistoryResponse;
import com.idp.dto.StrategyHistorySeriesResponse;
import com.idp.dto.StrategyIndicatorResponse;
import com.idp.dto.StrategyQuoteResponse;
import com.idp.dto.StrategyQuotesResponse;
import com.idp.exception.DecisionNotFoundException;
import com.idp.exception.StrategyConflictException;
import com.idp.exception.StrategyNotFoundException;
import com.idp.model.DecisionRecord;
import com.idp.model.StrategyPortfolio;
import com.idp.model.StrategyTrackedSymbol;
import com.idp.model.StrategyTransaction;
import com.idp.model.StrategyVisibility;
import com.idp.model.SymbolVisibility;
import com.idp.repository.DecisionRecordRepository;
import com.idp.repository.StrategyPortfolioRepository;
import com.idp.repository.StrategyTrackedSymbolRepository;
import com.idp.repository.StrategyTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class StrategyPortfolioService {
    private final StrategyPortfolioRepository strategyPortfolioRepository;
    private final StrategyTrackedSymbolRepository trackedSymbolRepository;
    private final StrategyTransactionRepository transactionRepository;
    private final DecisionRecordRepository decisionRecordRepository;
    private final MarketDataService marketDataService;

    public StrategyPortfolioService(
        StrategyPortfolioRepository strategyPortfolioRepository,
        StrategyTrackedSymbolRepository trackedSymbolRepository,
        StrategyTransactionRepository transactionRepository,
        DecisionRecordRepository decisionRecordRepository,
        MarketDataService marketDataService
    ) {
        this.strategyPortfolioRepository = strategyPortfolioRepository;
        this.trackedSymbolRepository = trackedSymbolRepository;
        this.transactionRepository = transactionRepository;
        this.decisionRecordRepository = decisionRecordRepository;
        this.marketDataService = marketDataService;
    }

    @Transactional
    public StrategyPortfolio create(String ownerId, CreateStrategyRequest request) {
        StrategyPortfolio strategy = new StrategyPortfolio();
        strategy.setOwnerId(ownerId);
        strategy.setName(request.name().trim());
        strategy.setDescription(request.description().trim());
        strategy.setStartingCapital(request.startingCapital());
        strategy.setVisibility(request.visibility());
        return strategyPortfolioRepository.save(strategy);
    }

    @Transactional(readOnly = true)
    public List<StrategyPortfolio> listOwned(String ownerId) {
        return strategyPortfolioRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    @Transactional(readOnly = true)
    public StrategyPortfolio getOwned(String ownerId, Long strategyId) {
        return findOwned(ownerId, strategyId);
    }

    @Transactional(readOnly = true)
    public StrategyPortfolio getPublic(Long strategyId) {
        return strategyPortfolioRepository.findByIdAndVisibility(strategyId, StrategyVisibility.PUBLIC)
            .orElseThrow(StrategyNotFoundException::new);
    }

    @Transactional
    public StrategyTrackedSymbol addTrackedSymbol(String ownerId, Long strategyId, AddTrackedSymbolRequest request) {
        StrategyPortfolio strategy = findOwned(ownerId, strategyId);
        String symbol = normalizeSymbol(request.symbol());
        if (trackedSymbolRepository.existsByStrategyIdAndSymbol(strategy.getId(), symbol)) {
            throw new StrategyConflictException("Symbol already tracked in this strategy");
        }

        StrategyTrackedSymbol trackedSymbol = new StrategyTrackedSymbol();
        trackedSymbol.setStrategy(strategy);
        trackedSymbol.setSymbol(symbol);
        trackedSymbol.setNote(request.note() == null || request.note().isBlank() ? null : request.note().trim());
        trackedSymbol.setTags(request.tags() == null ? List.of() : new ArrayList<>(request.tags()));
        trackedSymbol.setVisibility(request.visibility());
        return trackedSymbolRepository.save(trackedSymbol);
    }

    @Transactional
    public StrategyTransaction appendTransaction(String ownerId, Long strategyId, AppendTransactionRequest request) {
        StrategyPortfolio strategy = findOwned(ownerId, strategyId);
        if (request.decisionId() != null) {
            DecisionRecord decision = decisionRecordRepository.findByIdAndOwnerId(request.decisionId(), ownerId)
                .orElseThrow(DecisionNotFoundException::new);
            if (decision.getId() == null) {
                throw new DecisionNotFoundException();
            }
        }

        StrategyTransaction transaction = new StrategyTransaction();
        transaction.setStrategy(strategy);
        transaction.setSymbol(normalizeSymbol(request.ticker()));
        transaction.setSide(request.side());
        transaction.setQuantity(request.quantity());
        transaction.setPrice(request.price());
        transaction.setDecisionId(request.decisionId());
        transaction.setExecutedAt(request.executedAt());
        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<StrategyTrackedSymbol> trackedSymbols(Long strategyId, boolean publicOnly) {
        if (publicOnly) {
            return trackedSymbolRepository.findByStrategyIdAndVisibilityOrderBySymbol(strategyId, SymbolVisibility.PUBLIC);
        }
        return trackedSymbolRepository.findByStrategyIdOrderBySymbol(strategyId);
    }

    @Transactional(readOnly = true)
    public List<StrategyTransaction> transactions(Long strategyId) {
        return transactionRepository.findByStrategyIdOrderByExecutedAtAsc(strategyId);
    }

    @Transactional(readOnly = true)
    public StrategyQuotesResponse quotesForOwner(String ownerId, Long strategyId) {
        StrategyPortfolio strategy = findOwned(ownerId, strategyId);
        return quotes(strategy, false);
    }

    @Transactional(readOnly = true)
    public StrategyQuotesResponse quotesForPublic(Long strategyId) {
        StrategyPortfolio strategy = getPublic(strategyId);
        return quotes(strategy, true);
    }

    @Transactional(readOnly = true)
    public StrategyHistoryResponse historyForOwner(String ownerId, Long strategyId, String range) {
        StrategyPortfolio strategy = findOwned(ownerId, strategyId);
        return history(strategy, range, false);
    }

    @Transactional(readOnly = true)
    public StrategyHistoryResponse historyForPublic(Long strategyId, String range) {
        StrategyPortfolio strategy = getPublic(strategyId);
        return history(strategy, range, true);
    }

    @Transactional(readOnly = true)
    public StrategyIndicatorResponse indicatorsForOwner(String ownerId, Long strategyId, String symbol, String range) {
        StrategyPortfolio strategy = findOwned(ownerId, strategyId);
        return indicators(strategy, symbol, range, false);
    }

    private StrategyPortfolio findOwned(String ownerId, Long strategyId) {
        return strategyPortfolioRepository.findByIdAndOwnerId(strategyId, ownerId)
            .orElseThrow(StrategyNotFoundException::new);
    }

    private StrategyQuotesResponse quotes(StrategyPortfolio strategy, boolean publicOnly) {
        List<StrategyQuoteResponse> symbols = scopedSymbols(strategy.getId(), publicOnly).stream()
            .map(symbol -> marketDataService.quote(symbol)
                .map(quote -> new StrategyQuoteResponse(
                    symbol,
                    quote.lastPrice(),
                    quote.change(),
                    quote.percentChange(),
                    quote.marketTime(),
                    quote.marketState(),
                    quote.volume(),
                    null,
                    trackingStatus(strategy.getId(), symbol)
                ))
                .orElseGet(() -> new StrategyQuoteResponse(symbol, null, null, null, null, null, null, null, trackingStatus(strategy.getId(), symbol))))
            .toList();
        String dataFreshness = symbols.stream().anyMatch(symbol -> symbol.lastPrice() != null)
            ? "provider_yahoo_chart"
            : "market_data_unavailable";
        return new StrategyQuotesResponse(strategy.getId(), symbols, dataFreshness);
    }

    private StrategyHistoryResponse history(StrategyPortfolio strategy, String range, boolean publicOnly) {
        List<StrategyHistorySeriesResponse> series = scopedSymbols(strategy.getId(), publicOnly).stream()
            .map(symbol -> new StrategyHistorySeriesResponse(
                symbol,
                marketDataService.history(symbol, range).stream()
                    .map(point -> new StrategyHistoryPointResponse(point.timestamp(), point.close()))
                    .toList()
            ))
            .toList();
        return new StrategyHistoryResponse(strategy.getId(), range, series);
    }

    private StrategyIndicatorResponse indicators(StrategyPortfolio strategy, String rawSymbol, String range, boolean publicOnly) {
        String symbol = normalizeSymbol(rawSymbol);
        if (!scopedSymbols(strategy.getId(), publicOnly).contains(symbol)) {
            throw new StrategyNotFoundException();
        }
        List<MarketDataService.MarketHistoryPoint> points = marketDataService.history(symbol, range);
        BigDecimal rsi14 = rsi14(points);
        String verdict = trendVerdict(points, rsi14);
        String confidence = points.size() >= 50 ? "Medium" : points.size() >= 20 ? "Low" : "Low";
        return new StrategyIndicatorResponse(
            strategy.getId(),
            symbol,
            range,
            rsi14,
            verdict,
            confidence,
            points.size(),
            null,
            Map.of(
                "dataFreshness", points.isEmpty() ? "market_data_unavailable" : "provider_yahoo_chart",
                "closeCount", points.size()
            )
        );
    }

    private Set<String> scopedSymbols(Long strategyId, boolean publicOnly) {
        Set<String> symbols = new LinkedHashSet<>();
        List<StrategyTrackedSymbol> trackedSymbols = publicOnly
            ? trackedSymbolRepository.findByStrategyIdAndVisibilityOrderBySymbol(strategyId, SymbolVisibility.PUBLIC)
            : trackedSymbolRepository.findByStrategyIdOrderBySymbol(strategyId);
        trackedSymbols.forEach(trackedSymbol -> symbols.add(trackedSymbol.getSymbol()));
        transactionRepository.findByStrategyIdOrderByExecutedAtAsc(strategyId)
            .forEach(transaction -> symbols.add(transaction.getSymbol()));
        return symbols;
    }

    private String trackingStatus(Long strategyId, String symbol) {
        if (transactionRepository.existsByStrategyIdAndSymbol(strategyId, symbol)) {
            return "position";
        }
        return "tracked";
    }

    private String normalizeSymbol(String symbol) {
        return symbol.trim().toUpperCase(Locale.US);
    }

    private BigDecimal rsi14(List<MarketDataService.MarketHistoryPoint> points) {
        if (points.size() < 15) {
            return null;
        }
        BigDecimal gain = BigDecimal.ZERO;
        BigDecimal loss = BigDecimal.ZERO;
        List<MarketDataService.MarketHistoryPoint> tail = points.subList(Math.max(1, points.size() - 14), points.size());
        for (int i = 1; i < tail.size(); i++) {
            BigDecimal previous = tail.get(i - 1).close();
            BigDecimal current = tail.get(i).close();
            BigDecimal delta = current.subtract(previous);
            if (delta.signum() >= 0) {
                gain = gain.add(delta);
            } else {
                loss = loss.add(delta.abs());
            }
        }
        if (loss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        BigDecimal relativeStrength = gain.divide(loss, 8, java.math.RoundingMode.HALF_UP);
        BigDecimal rsi = BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(relativeStrength), 4, java.math.RoundingMode.HALF_UP));
        return rsi.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String trendVerdict(List<MarketDataService.MarketHistoryPoint> points, BigDecimal rsi14) {
        if (points.size() < 20 || rsi14 == null) {
            return "No Validated Edge";
        }
        BigDecimal latest = points.get(points.size() - 1).close();
        BigDecimal previous = points.get(Math.max(0, points.size() - 20)).close();
        int priceDirection = latest.compareTo(previous);
        if (priceDirection > 0 && rsi14.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return "Bullish Trend";
        }
        if (priceDirection < 0 && rsi14.compareTo(BigDecimal.valueOf(50)) <= 0) {
            return "Bearish Trend";
        }
        return "Mixed Trend";
    }
}
