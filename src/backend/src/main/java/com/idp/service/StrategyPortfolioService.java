package com.idp.service;

import com.idp.dto.AddTrackedSymbolRequest;
import com.idp.dto.AppendTransactionRequest;
import com.idp.dto.CreateStrategyRequest;
import com.idp.dto.StrategyHistoryPointResponse;
import com.idp.dto.StrategyHistoryResponse;
import com.idp.dto.StrategyHistorySeriesResponse;
import com.idp.dto.StrategyIndicatorResponse;
import com.idp.dto.StrategyPerformancePointResponse;
import com.idp.dto.StrategyQuoteResponse;
import com.idp.dto.StrategyQuotesResponse;
import com.idp.dto.UpdateStrategyVisibilityRequest;
import com.idp.exception.DecisionNotFoundException;
import com.idp.exception.StrategyConflictException;
import com.idp.exception.StrategyNotFoundException;
import com.idp.model.DecisionRecord;
import com.idp.model.DecisionType;
import com.idp.model.StrategyPortfolio;
import com.idp.model.StrategyTrackedSymbol;
import com.idp.model.StrategyTransaction;
import com.idp.model.StrategyVisibility;
import com.idp.model.SymbolVisibility;
import com.idp.model.TransactionSide;
import com.idp.repository.DecisionRecordRepository;
import com.idp.repository.StrategyPortfolioRepository;
import com.idp.repository.StrategyTrackedSymbolRepository;
import com.idp.repository.StrategyTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Service
public class StrategyPortfolioService {
    private final StrategyPortfolioRepository strategyPortfolioRepository;
    private final StrategyTrackedSymbolRepository trackedSymbolRepository;
    private final StrategyTransactionRepository transactionRepository;
    private final DecisionRecordRepository decisionRecordRepository;
    private final MarketDataService marketDataService;
    private final InvestmentDecisionService decisionService;

    public StrategyPortfolioService(
        StrategyPortfolioRepository strategyPortfolioRepository,
        StrategyTrackedSymbolRepository trackedSymbolRepository,
        StrategyTransactionRepository transactionRepository,
        DecisionRecordRepository decisionRecordRepository,
        MarketDataService marketDataService,
        InvestmentDecisionService decisionService
    ) {
        this.strategyPortfolioRepository = strategyPortfolioRepository;
        this.trackedSymbolRepository = trackedSymbolRepository;
        this.transactionRepository = transactionRepository;
        this.decisionRecordRepository = decisionRecordRepository;
        this.marketDataService = marketDataService;
        this.decisionService = decisionService;
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
    public StrategyPortfolio updateVisibility(String ownerId, Long strategyId, UpdateStrategyVisibilityRequest request) {
        StrategyPortfolio strategy = findOwned(ownerId, strategyId);
        strategy.setVisibility(request.visibility());
        return strategyPortfolioRepository.save(strategy);
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
    public void removeTrackedSymbol(String ownerId, Long strategyId, String rawSymbol) {
        StrategyPortfolio strategy = findOwned(ownerId, strategyId);
        String symbol = normalizeSymbol(rawSymbol);
        if (transactionRepository.existsByStrategyIdAndSymbol(strategy.getId(), symbol)) {
            throw new StrategyConflictException("Symbol has transaction history and cannot be deleted from strategy scope");
        }
        StrategyTrackedSymbol trackedSymbol = trackedSymbolRepository.findByStrategyIdAndSymbol(strategy.getId(), symbol)
            .orElseThrow(StrategyNotFoundException::new);
        trackedSymbolRepository.delete(trackedSymbol);
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

        String symbol = normalizeSymbol(request.ticker());
        MarketDataService.MarketQuote quote = marketDataService.quote(symbol)
            .orElseThrow(() -> new StrategyConflictException("Live quote unavailable for " + symbol));
        if (!isRegularMarket(quote) || quote.lastPrice() == null || quote.lastPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new StrategyConflictException("Buy/sell is only available during regular market hours with a live quote");
        }

        BigDecimal executionPrice = money(quote.lastPrice());
        BigDecimal tradeValue = money(request.quantity().multiply(executionPrice));
        Map<String, PositionTotals> currentPositions = positions(strategy.getId());
        BigDecimal cashBalance = cashBalance(strategy);

        if (request.side() == TransactionSide.BUY && tradeValue.compareTo(cashBalance) > 0) {
            throw new StrategyConflictException("Insufficient strategy cash for this buy");
        }
        if (request.side() == TransactionSide.SELL) {
            BigDecimal ownedQuantity = currentPositions.getOrDefault(symbol, PositionTotals.empty()).quantity();
            if (ownedQuantity.compareTo(request.quantity()) < 0) {
                throw new StrategyConflictException("Insufficient owned shares for this sell");
            }
        }

        StrategyTransaction transaction = new StrategyTransaction();
        transaction.setStrategy(strategy);
        transaction.setSymbol(symbol);
        transaction.setSide(request.side());
        transaction.setQuantity(request.quantity());
        transaction.setPrice(executionPrice);
        transaction.setDecisionId(request.decisionId());
        transaction.setExecutedAt(request.executedAt());
        transaction = transactionRepository.save(transaction);

        // Capture AUTO decision with real-time execution price (user cannot override)
        DecisionType decisionType = request.side() == TransactionSide.BUY ? DecisionType.BUY : DecisionType.SELL;
        LocalDate transactionDate = request.executedAt().atZone(ZoneId.systemDefault()).toLocalDate();
        decisionService.createAutoDecision(
            strategy.getOwnerId(), symbol, decisionType, request.quantity(), executionPrice, transactionDate);

        return transaction;
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
        Map<String, PositionTotals> positions = positions(strategy.getId());
        Map<String, MarketDataService.MarketQuote> quotes = new HashMap<>();
        scopedSymbols(strategy.getId(), publicOnly).forEach(symbol -> marketDataService.quote(symbol).ifPresent(quote -> quotes.put(symbol, quote)));
        BigDecimal holdingsValue = positions.entrySet().stream()
            .map(entry -> marketValue(entry.getValue(), quotes.get(entry.getKey())))
            .filter(value -> value != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cashBalance = cashBalance(strategy);
        BigDecimal totalStrategyValue = money(cashBalance.add(holdingsValue));
        BigDecimal totalGain = money(totalStrategyValue.subtract(strategy.getStartingCapital()));
        BigDecimal totalGainPct = strategy.getStartingCapital().compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : totalGain.multiply(BigDecimal.valueOf(100)).divide(strategy.getStartingCapital(), 4, RoundingMode.HALF_UP);
        List<StrategyQuoteResponse> symbols = scopedSymbols(strategy.getId(), publicOnly).stream()
            .map(symbol -> quoteResponse(symbol, quotes.get(symbol), positions.get(symbol), totalStrategyValue))
            .toList();
        String dataFreshness = symbols.stream().anyMatch(symbol -> symbol.lastPrice() != null)
            ? "provider_yahoo_chart"
            : "market_data_unavailable";
        return new StrategyQuotesResponse(
            strategy.getId(),
            symbols,
            money(strategy.getStartingCapital()),
            cashBalance,
            money(holdingsValue),
            totalStrategyValue,
            totalGain,
            totalGainPct,
            dataFreshness
        );
    }

    private StrategyHistoryResponse history(StrategyPortfolio strategy, String range, boolean publicOnly) {
        Map<String, List<MarketDataService.MarketHistoryPoint>> histories = new HashMap<>();
        scopedSymbols(strategy.getId(), publicOnly).forEach(symbol -> histories.put(symbol, marketDataService.history(symbol, range)));
        List<StrategyHistorySeriesResponse> series = histories.entrySet().stream()
            .map(symbol -> new StrategyHistorySeriesResponse(
                symbol.getKey(),
                symbol.getValue().stream()
                    .map(point -> new StrategyHistoryPointResponse(point.timestamp(), point.close()))
                    .toList()
            ))
            .toList();
        return new StrategyHistoryResponse(strategy.getId(), range, series, performanceHistory(strategy, histories));
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

    private StrategyQuoteResponse quoteResponse(
        String symbol,
        MarketDataService.MarketQuote quote,
        PositionTotals position,
        BigDecimal totalStrategyValue
    ) {
        BigDecimal marketValue = marketValue(position, quote);
        BigDecimal unrealizedGain = marketValue == null || position == null ? null : money(marketValue.subtract(position.costBasis()));
        BigDecimal unrealizedGainPct = unrealizedGain == null || position.costBasis().compareTo(BigDecimal.ZERO) == 0
            ? null
            : unrealizedGain.multiply(BigDecimal.valueOf(100)).divide(position.costBasis(), 4, RoundingMode.HALF_UP);
        BigDecimal positionWeight = marketValue == null || totalStrategyValue == null || totalStrategyValue.compareTo(BigDecimal.ZERO) == 0
            ? null
            : marketValue.multiply(BigDecimal.valueOf(100)).divide(totalStrategyValue, 4, RoundingMode.HALF_UP);
        return new StrategyQuoteResponse(
            symbol,
            quote == null ? null : quote.lastPrice(),
            quote == null ? null : quote.change(),
            quote == null ? null : quote.percentChange(),
            quote == null ? null : quote.marketTime(),
            quote == null ? null : quote.marketState(),
            quote == null ? null : quote.volume(),
            position == null ? null : position.quantity(),
            position == null ? null : position.averageCost(),
            position == null ? null : position.costBasis(),
            marketValue,
            unrealizedGain,
            unrealizedGainPct,
            positionWeight,
            position == null ? "watch" : "owned"
        );
    }

    private Map<String, PositionTotals> positions(Long strategyId) {
        Map<String, PositionAccumulator> accumulators = new HashMap<>();
        transactionRepository.findByStrategyIdOrderByExecutedAtAsc(strategyId).forEach(transaction -> {
            PositionAccumulator accumulator = accumulators.computeIfAbsent(transaction.getSymbol(), symbol -> new PositionAccumulator());
            BigDecimal quantity = transaction.getQuantity();
            BigDecimal value = transaction.getQuantity().multiply(transaction.getPrice());
            switch (transaction.getSide()) {
                case BUY -> {
                    accumulator.quantity = accumulator.quantity.add(quantity);
                    accumulator.costBasis = accumulator.costBasis.add(value);
                }
                case SELL -> {
                    BigDecimal averageCost = accumulator.quantity.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : accumulator.costBasis.divide(accumulator.quantity, 8, RoundingMode.HALF_UP);
                    accumulator.quantity = accumulator.quantity.subtract(quantity);
                    accumulator.costBasis = accumulator.costBasis.subtract(averageCost.multiply(quantity));
                    if (accumulator.quantity.compareTo(BigDecimal.ZERO) <= 0) {
                        accumulator.quantity = BigDecimal.ZERO;
                        accumulator.costBasis = BigDecimal.ZERO;
                    }
                }
            }
        });
        Map<String, PositionTotals> positions = new HashMap<>();
        accumulators.forEach((symbol, accumulator) -> {
            if (accumulator.quantity.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal averageCost = accumulator.costBasis.divide(accumulator.quantity, 6, RoundingMode.HALF_UP);
                positions.put(symbol, new PositionTotals(accumulator.quantity, averageCost, money(accumulator.costBasis)));
            }
        });
        return positions;
    }

    private BigDecimal cashBalance(StrategyPortfolio strategy) {
        BigDecimal cash = strategy.getStartingCapital();
        for (StrategyTransaction transaction : transactionRepository.findByStrategyIdOrderByExecutedAtAsc(strategy.getId())) {
            BigDecimal value = money(transaction.getQuantity().multiply(transaction.getPrice()));
            if (transaction.getSide() == TransactionSide.BUY) {
                cash = cash.subtract(value);
            } else {
                cash = cash.add(value);
            }
        }
        return money(cash);
    }

    private List<StrategyPerformancePointResponse> performanceHistory(
        StrategyPortfolio strategy,
        Map<String, List<MarketDataService.MarketHistoryPoint>> histories
    ) {
        List<StrategyTransaction> transactions = transactionRepository.findByStrategyIdOrderByExecutedAtAsc(strategy.getId());
        Set<String> transactionSymbols = new LinkedHashSet<>();
        transactions.forEach(transaction -> transactionSymbols.add(transaction.getSymbol()));

        TreeMap<Long, Map<String, BigDecimal>> closesByTimestamp = new TreeMap<>();
        transactionSymbols.forEach(symbol -> histories.getOrDefault(symbol, List.of()).forEach(point ->
            closesByTimestamp.computeIfAbsent(point.timestamp(), ignored -> new HashMap<>()).put(symbol, point.close())
        ));
        if (closesByTimestamp.isEmpty()) {
            return List.of(new StrategyPerformancePointResponse(
                Instant.now().toEpochMilli(),
                money(strategy.getStartingCapital()),
                money(strategy.getStartingCapital()),
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP)
            ));
        }

        List<StrategyPerformancePointResponse> performance = new ArrayList<>();
        Map<String, BigDecimal> latestClose = new HashMap<>();
        int transactionIndex = 0;
        BigDecimal cash = strategy.getStartingCapital();
        Map<String, PositionAccumulator> positions = new HashMap<>();

        for (Map.Entry<Long, Map<String, BigDecimal>> entry : closesByTimestamp.entrySet()) {
            latestClose.putAll(entry.getValue());
            while (transactionIndex < transactions.size()
                && transactions.get(transactionIndex).getExecutedAt().toEpochMilli() <= entry.getKey()) {
                StrategyTransaction transaction = transactions.get(transactionIndex);
                BigDecimal value = money(transaction.getQuantity().multiply(transaction.getPrice()));
                PositionAccumulator accumulator = positions.computeIfAbsent(transaction.getSymbol(), ignored -> new PositionAccumulator());
                if (transaction.getSide() == TransactionSide.BUY) {
                    cash = cash.subtract(value);
                    accumulator.quantity = accumulator.quantity.add(transaction.getQuantity());
                    accumulator.costBasis = accumulator.costBasis.add(value);
                } else {
                    cash = cash.add(value);
                    BigDecimal averageCost = accumulator.quantity.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : accumulator.costBasis.divide(accumulator.quantity, 8, RoundingMode.HALF_UP);
                    accumulator.quantity = accumulator.quantity.subtract(transaction.getQuantity());
                    accumulator.costBasis = accumulator.costBasis.subtract(averageCost.multiply(transaction.getQuantity()));
                    if (accumulator.quantity.compareTo(BigDecimal.ZERO) <= 0) {
                        accumulator.quantity = BigDecimal.ZERO;
                        accumulator.costBasis = BigDecimal.ZERO;
                    }
                }
                transactionIndex++;
            }

            BigDecimal holdingsValue = positions.entrySet().stream()
                .filter(position -> position.getValue().quantity.compareTo(BigDecimal.ZERO) > 0)
                .map(position -> position.getValue().quantity.multiply(latestClose.getOrDefault(position.getKey(), BigDecimal.ZERO)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalValue = money(cash.add(holdingsValue));
            BigDecimal returnPct = strategy.getStartingCapital().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalValue.subtract(strategy.getStartingCapital()).multiply(BigDecimal.valueOf(100)).divide(strategy.getStartingCapital(), 4, RoundingMode.HALF_UP);
            performance.add(new StrategyPerformancePointResponse(
                entry.getKey(),
                totalValue,
                money(cash),
                money(holdingsValue),
                returnPct
            ));
        }
        return performance;
    }

    private BigDecimal marketValue(PositionTotals position, MarketDataService.MarketQuote quote) {
        if (position == null || quote == null || quote.lastPrice() == null) {
            return null;
        }
        return money(position.quantity().multiply(quote.lastPrice()));
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeSymbol(String symbol) {
        return symbol.trim().toUpperCase(Locale.US);
    }

    private boolean isRegularMarket(MarketDataService.MarketQuote quote) {
        if (quote.marketState() == null) {
            return false;
        }
        String state = quote.marketState().trim().toUpperCase(Locale.US);
        return state.equals("REGULAR") || state.equals("OPEN");
    }

    private static class PositionAccumulator {
        private BigDecimal quantity = BigDecimal.ZERO;
        private BigDecimal costBasis = BigDecimal.ZERO;
    }

    private record PositionTotals(BigDecimal quantity, BigDecimal averageCost, BigDecimal costBasis) {
        private static PositionTotals empty() {
            return new PositionTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
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
