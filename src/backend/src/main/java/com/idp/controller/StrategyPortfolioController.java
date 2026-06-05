package com.idp.controller;

import com.idp.dto.AddTrackedSymbolRequest;
import com.idp.dto.AppendTransactionRequest;
import com.idp.dto.AppendTransactionResponse;
import com.idp.dto.CreateStrategyRequest;
import com.idp.dto.CreateStrategyResponse;
import com.idp.dto.StrategyDetailResponse;
import com.idp.dto.StrategyHistoryResponse;
import com.idp.dto.StrategyIndicatorResponse;
import com.idp.dto.StrategyListResponse;
import com.idp.dto.StrategyQuotesResponse;
import com.idp.dto.StrategySummaryResponse;
import com.idp.dto.StrategyTransactionResponse;
import com.idp.dto.TrackedSymbolResponse;
import com.idp.model.StrategyPortfolio;
import com.idp.model.StrategyTrackedSymbol;
import com.idp.model.StrategyTransaction;
import com.idp.service.StrategyPortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/strategies")
public class StrategyPortfolioController {
    private final StrategyPortfolioService strategyPortfolioService;

    public StrategyPortfolioController(StrategyPortfolioService strategyPortfolioService) {
        this.strategyPortfolioService = strategyPortfolioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateStrategyResponse create(@Valid @RequestBody CreateStrategyRequest request, Authentication authentication) {
        StrategyPortfolio strategy = strategyPortfolioService.create(authentication.getName(), request);
        return new CreateStrategyResponse(strategy.getId(), strategy.getName(), strategy.getVisibility(), strategy.getCreatedAt());
    }

    @GetMapping
    public StrategyListResponse list(Authentication authentication) {
        return new StrategyListResponse(strategyPortfolioService.listOwned(authentication.getName()).stream()
            .map(strategy -> new StrategySummaryResponse(
                strategy.getId(),
                strategy.getName(),
                strategy.getStartingCapital(),
                strategy.getVisibility(),
                strategy.getCreatedAt()
            ))
            .toList());
    }

    @GetMapping("/{id}")
    public StrategyDetailResponse get(@PathVariable("id") Long id, Authentication authentication) {
        StrategyPortfolio strategy = strategyPortfolioService.getOwned(authentication.getName(), id);
        return detail(strategy, false);
    }

    @PostMapping("/{id}/symbols")
    @ResponseStatus(HttpStatus.CREATED)
    public TrackedSymbolResponse addSymbol(
        @PathVariable("id") Long id,
        @Valid @RequestBody AddTrackedSymbolRequest request,
        Authentication authentication
    ) {
        return trackedSymbol(strategyPortfolioService.addTrackedSymbol(authentication.getName(), id, request));
    }

    @PostMapping("/{id}/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public AppendTransactionResponse appendTransaction(
        @PathVariable("id") Long id,
        @Valid @RequestBody AppendTransactionRequest request,
        Authentication authentication
    ) {
        StrategyTransaction transaction = strategyPortfolioService.appendTransaction(authentication.getName(), id, request);
        return new AppendTransactionResponse(transaction.getId(), transaction.getStrategy().getId(), transaction.getCreatedAt());
    }

    @GetMapping("/{id}/quotes")
    public StrategyQuotesResponse quotes(@PathVariable("id") Long id, Authentication authentication) {
        return strategyPortfolioService.quotesForOwner(authentication.getName(), id);
    }

    @GetMapping("/{id}/history")
    public StrategyHistoryResponse history(
        @PathVariable("id") Long id,
        @RequestParam(name = "range") String range,
        Authentication authentication
    ) {
        return strategyPortfolioService.historyForOwner(authentication.getName(), id, range);
    }

    @GetMapping("/{id}/indicators")
    public StrategyIndicatorResponse indicators(
        @PathVariable("id") Long id,
        @RequestParam(name = "symbol") String symbol,
        @RequestParam(name = "range") String range,
        Authentication authentication
    ) {
        return strategyPortfolioService.indicatorsForOwner(authentication.getName(), id, symbol, range);
    }

    private StrategyDetailResponse detail(StrategyPortfolio strategy, boolean publicOnly) {
        return new StrategyDetailResponse(
            strategy.getId(),
            strategy.getName(),
            strategy.getDescription(),
            strategy.getStartingCapital(),
            strategy.getVisibility(),
            strategyPortfolioService.trackedSymbols(strategy.getId(), publicOnly).stream().map(this::trackedSymbol).toList(),
            strategyPortfolioService.transactions(strategy.getId()).stream().map(this::transaction).toList(),
            strategy.getCreatedAt()
        );
    }

    private TrackedSymbolResponse trackedSymbol(StrategyTrackedSymbol trackedSymbol) {
        return new TrackedSymbolResponse(
            trackedSymbol.getStrategy().getId(),
            trackedSymbol.getSymbol(),
            "tracked",
            trackedSymbol.getNote(),
            trackedSymbol.getTags(),
            trackedSymbol.getVisibility(),
            trackedSymbol.getCreatedAt()
        );
    }

    private StrategyTransactionResponse transaction(StrategyTransaction transaction) {
        return new StrategyTransactionResponse(
            transaction.getId(),
            transaction.getStrategy().getId(),
            transaction.getSymbol(),
            transaction.getSide(),
            transaction.getQuantity(),
            transaction.getPrice(),
            transaction.getDecisionId(),
            transaction.getExecutedAt(),
            transaction.getCreatedAt()
        );
    }
}
