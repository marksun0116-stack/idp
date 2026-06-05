package com.idp.controller;

import com.idp.dto.StrategyDetailResponse;
import com.idp.dto.StrategyHistoryResponse;
import com.idp.dto.StrategyQuotesResponse;
import com.idp.model.StrategyPortfolio;
import com.idp.service.StrategyPortfolioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/strategies")
public class PublicStrategyController {
    private final StrategyPortfolioService strategyPortfolioService;

    public PublicStrategyController(StrategyPortfolioService strategyPortfolioService) {
        this.strategyPortfolioService = strategyPortfolioService;
    }

    @GetMapping("/{id}")
    public StrategyDetailResponse get(@PathVariable("id") Long id) {
        StrategyPortfolio strategy = strategyPortfolioService.getPublic(id);
        return new StrategyDetailResponse(
            strategy.getId(),
            strategy.getName(),
            strategy.getDescription(),
            strategy.getStartingCapital(),
            strategy.getVisibility(),
            strategyPortfolioService.trackedSymbols(strategy.getId(), true).stream()
                .map(symbol -> new com.idp.dto.TrackedSymbolResponse(
                    symbol.getStrategy().getId(),
                    symbol.getSymbol(),
                    "tracked",
                    symbol.getNote(),
                    symbol.getTags(),
                    symbol.getVisibility(),
                    symbol.getCreatedAt()
                ))
                .toList(),
            strategyPortfolioService.transactions(strategy.getId()).stream()
                .map(transaction -> new com.idp.dto.StrategyTransactionResponse(
                    transaction.getId(),
                    transaction.getStrategy().getId(),
                    transaction.getSymbol(),
                    transaction.getSide(),
                    transaction.getQuantity(),
                    transaction.getPrice(),
                    transaction.getDecisionId(),
                    transaction.getExecutedAt(),
                    transaction.getCreatedAt()
                ))
                .toList(),
            strategy.getCreatedAt()
        );
    }

    @GetMapping("/{id}/quotes")
    public StrategyQuotesResponse quotes(@PathVariable("id") Long id) {
        return strategyPortfolioService.quotesForPublic(id);
    }

    @GetMapping("/{id}/history")
    public StrategyHistoryResponse history(@PathVariable("id") Long id, @RequestParam(name = "range") String range) {
        return strategyPortfolioService.historyForPublic(id, range);
    }
}
