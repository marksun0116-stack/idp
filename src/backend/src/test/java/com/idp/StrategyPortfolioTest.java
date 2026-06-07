package com.idp;

// Traces: US-007, US-008, US-009, US-010, CONR-strategy-portfolio-api-001, INV-strategy-market-data-scope-001

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idp.repository.DecisionRecordRepository;
import com.idp.repository.DecisionReviewRepository;
import com.idp.repository.DecisionRevisionRepository;
import com.idp.repository.StrategyPortfolioRepository;
import com.idp.repository.StrategyTrackedSymbolRepository;
import com.idp.repository.StrategyTransactionRepository;
import com.idp.service.MarketDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StrategyPortfolioTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired StrategyPortfolioRepository strategyPortfolioRepository;
    @Autowired StrategyTrackedSymbolRepository trackedSymbolRepository;
    @Autowired StrategyTransactionRepository transactionRepository;
    @Autowired DecisionRecordRepository decisionRecordRepository;
    @Autowired DecisionRevisionRepository decisionRevisionRepository;
    @Autowired DecisionReviewRepository decisionReviewRepository;

    @MockBean MarketDataService marketDataService;

    @BeforeEach
    void cleanUp() {
        transactionRepository.deleteAll();
        trackedSymbolRepository.deleteAll();
        strategyPortfolioRepository.deleteAll();
        decisionReviewRepository.deleteAll();
        decisionRevisionRepository.deleteAll();
        decisionRecordRepository.deleteAll();
        when(marketDataService.quote(anyString())).thenReturn(Optional.empty());
        when(marketDataService.history(anyString(), anyString())).thenReturn(List.of());
    }

    @Test
    void createStrategy_returnsOwnerScopedStrategy() throws Exception {
        long strategyId = createStrategy("alice", "Core AI Strategy", "public");

        mvc.perform(get("/api/strategies").header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.strategies.length()").value(1))
            .andExpect(jsonPath("$.strategies[0].id").value(strategyId))
            .andExpect(jsonPath("$.strategies[0].name").value("Core AI Strategy"));

        mvc.perform(get("/api/strategies").header("Authorization", "Bearer bob"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.strategies.length()").value(0));

        mvc.perform(get("/api/strategies/" + strategyId).header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(strategyId))
            .andExpect(jsonPath("$.name").value("Core AI Strategy"))
            .andExpect(jsonPath("$.visibility").value("public"));

        mvc.perform(get("/api/strategies/" + strategyId).header("Authorization", "Bearer bob"))
            .andExpect(status().isNotFound());
    }

    @Test
    void addTrackedSymbolDoesNotCreateTransactionAndRejectsDuplicate() throws Exception {
        long strategyId = createStrategy("alice", "Research Strategy", "private");

        mvc.perform(post("/api/strategies/" + strategyId + "/symbols")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "symbol": "nvda",
                      "note": "Watch AI infrastructure demand",
                      "tags": ["ai", "semis"],
                      "visibility": "private"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.strategyId").value(strategyId))
            .andExpect(jsonPath("$.symbol").value("NVDA"))
            .andExpect(jsonPath("$.trackingStatus").value("watch"));

        assertThat(transactionRepository.findByStrategyIdOrderByExecutedAtAsc(strategyId)).isEmpty();

        mvc.perform(post("/api/strategies/" + strategyId + "/symbols")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "symbol": "NVDA",
                      "note": "Duplicate",
                      "tags": [],
                      "visibility": "private"
                    }
                    """))
            .andExpect(status().isConflict());
    }

    @Test
    void removeWatchSymbolDeletesOnlySymbolsWithoutTransactionHistory() throws Exception {
        long strategyId = createStrategy("alice", "Research Strategy", "private");
        addSymbol("alice", strategyId, "NVDA", "private");

        mvc.perform(delete("/api/strategies/" + strategyId + "/symbols/NVDA")
                .header("Authorization", "Bearer alice"))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/strategies/" + strategyId + "/quotes").header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbols.length()").value(0));

        addSymbol("alice", strategyId, "NVDA", "private");
        mockRegularQuote("NVDA", 100);
        mvc.perform(post("/api/strategies/" + strategyId + "/transactions")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "ticker": "NVDA",
                      "side": "buy",
                      "quantity": 1,
                      "decisionId": null,
                      "executedAt": "2026-06-05T08:00:00Z"
                    }
                    """))
            .andExpect(status().isCreated());

        mvc.perform(delete("/api/strategies/" + strategyId + "/symbols/NVDA")
                .header("Authorization", "Bearer alice"))
            .andExpect(status().isConflict());
    }

    @Test
    void appendTransactionCreatesImmutableEventAndValidatesDecisionOwner() throws Exception {
        long strategyId = createStrategy("alice", "Public Strategy", "public");
        long decisionId = createDecision("alice", "NVDA");
        long bobDecisionId = createDecision("bob", "MSFT");
        mockRegularQuote("NVDA", 125.50);

        mvc.perform(post("/api/strategies/" + strategyId + "/transactions")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(transactionBody("NVDA", decisionId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.strategyId").value(strategyId));

        assertThat(transactionRepository.findByStrategyIdOrderByExecutedAtAsc(strategyId)).hasSize(1);

        mvc.perform(post("/api/strategies/" + strategyId + "/transactions")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(transactionBody("MSFT", bobDecisionId)))
            .andExpect(status().isNotFound());
    }

    @Test
    void marketDataSurfacesAreScopedToStrategySymbols() throws Exception {
        long strategyId = createStrategy("alice", "Research Strategy", "private");
        addSymbol("alice", strategyId, "NVDA", "private");

        mvc.perform(get("/api/strategies/" + strategyId + "/quotes").header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbols.length()").value(1))
            .andExpect(jsonPath("$.symbols[0].symbol").value("NVDA"))
            .andExpect(jsonPath("$.dataFreshness").value("market_data_unavailable"));

        mvc.perform(get("/api/strategies/" + strategyId + "/history")
                .header("Authorization", "Bearer alice")
                .queryParam("range", "1mo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.series[0].symbol").value("NVDA"));

        mvc.perform(get("/api/strategies/" + strategyId + "/indicators")
                .header("Authorization", "Bearer alice")
                .queryParam("symbol", "NVDA")
                .queryParam("range", "1y"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trendVerdict").value("No Validated Edge"));

        mvc.perform(get("/api/strategies/" + strategyId + "/indicators")
                .header("Authorization", "Bearer alice")
                .queryParam("symbol", "AAPL")
                .queryParam("range", "1y"))
            .andExpect(status().isNotFound());
    }

    @Test
    void publicStrategyReadsRespectVisibilityAndPublicTrackedSymbols() throws Exception {
        long publicStrategyId = createStrategy("alice", "Published Strategy", "public");
        long privateStrategyId = createStrategy("alice", "Private Strategy", "private");
        addSymbol("alice", publicStrategyId, "NVDA", "public");
        addSymbol("alice", publicStrategyId, "TSLA", "private");

        mvc.perform(get("/api/public/strategies/" + publicStrategyId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trackedSymbols.length()").value(1))
            .andExpect(jsonPath("$.trackedSymbols[0].symbol").value("NVDA"));

        mvc.perform(get("/api/public/strategies/" + privateStrategyId))
            .andExpect(status().isNotFound());

        mvc.perform(get("/api/public/strategies/" + publicStrategyId + "/quotes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbols.length()").value(1))
            .andExpect(jsonPath("$.symbols[0].symbol").value("NVDA"));
    }

    @Test
    void updateStrategyVisibilitySwitchesPublicAccess() throws Exception {
        long strategyId = createStrategy("alice", "Visibility Strategy", "private");

        mvc.perform(get("/api/public/strategies/" + strategyId))
            .andExpect(status().isNotFound());

        mvc.perform(put("/api/strategies/" + strategyId + "/visibility")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "visibility": "public" }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.visibility").value("public"));

        mvc.perform(get("/api/public/strategies/" + strategyId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.visibility").value("public"));

        mvc.perform(put("/api/strategies/" + strategyId + "/visibility")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "visibility": "private" }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.visibility").value("private"));

        mvc.perform(get("/api/public/strategies/" + strategyId))
            .andExpect(status().isNotFound());
    }

    @Test
    void positionQuotesIncludeTransactionDerivedAllocationAndPerformance() throws Exception {
        long strategyId = createStrategy("alice", "Position Strategy", "private");
        mockRegularQuote("NVDA", 150);
        mvc.perform(post("/api/strategies/" + strategyId + "/transactions")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "ticker": "NVDA",
                      "side": "buy",
                      "quantity": 10,
                      "price": 100,
                      "decisionId": null,
                      "executedAt": "2026-06-05T08:00:00Z"
                    }
                    """))
            .andExpect(status().isCreated());

        mvc.perform(get("/api/strategies/" + strategyId + "/quotes").header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbols[0].symbol").value("NVDA"))
            .andExpect(jsonPath("$.symbols[0].trackingStatus").value("owned"))
            .andExpect(jsonPath("$.symbols[0].quantity").value(10))
            .andExpect(jsonPath("$.symbols[0].averageCost").value(150.000000))
            .andExpect(jsonPath("$.symbols[0].costBasis").value(1500.00))
            .andExpect(jsonPath("$.symbols[0].marketValue").value(1500.00))
            .andExpect(jsonPath("$.symbols[0].unrealizedGain").value(0.00))
            .andExpect(jsonPath("$.symbols[0].unrealizedGainPct").value(0.0000))
            .andExpect(jsonPath("$.symbols[0].positionWeight").value(1.5000))
            .andExpect(jsonPath("$.cashBalance").value(98500.00))
            .andExpect(jsonPath("$.holdingsValue").value(1500.00))
            .andExpect(jsonPath("$.totalStrategyValue").value(100000.00))
            .andExpect(jsonPath("$.totalGainPct").value(0.0000));
    }

    @Test
    void transactionRulesRequireRegularMarketCashAndOwnedShares() throws Exception {
        long strategyId = createStrategy("alice", "Guardrail Strategy", "private");
        mockClosedQuote("NVDA", 100);

        mvc.perform(post("/api/strategies/" + strategyId + "/transactions")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "ticker": "NVDA",
                      "side": "buy",
                      "quantity": 1,
                      "decisionId": null,
                      "executedAt": "2026-06-05T08:00:00Z"
                    }
                    """))
            .andExpect(status().isConflict());

        mockRegularQuote("NVDA", 1000);
        mvc.perform(post("/api/strategies/" + strategyId + "/transactions")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "ticker": "NVDA",
                      "side": "buy",
                      "quantity": 101,
                      "decisionId": null,
                      "executedAt": "2026-06-05T08:00:00Z"
                    }
                    """))
            .andExpect(status().isConflict());

        mvc.perform(post("/api/strategies/" + strategyId + "/transactions")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "ticker": "NVDA",
                      "side": "sell",
                      "quantity": 1,
                      "decisionId": null,
                      "executedAt": "2026-06-05T08:00:00Z"
                    }
                    """))
            .andExpect(status().isConflict());
    }

    @Test
    void historyIncludesTotalStrategyPerformanceWithCash() throws Exception {
        long strategyId = createStrategy("alice", "Performance Strategy", "private");
        mockRegularQuote("NVDA", 120);
        when(marketDataService.history(eq("NVDA"), eq("1mo"))).thenReturn(List.of(
            new MarketDataService.MarketHistoryPoint(1_780_000_000_000L, BigDecimal.valueOf(100), BigDecimal.valueOf(102), BigDecimal.valueOf(98), BigDecimal.valueOf(100), 1_000_000L),
            new MarketDataService.MarketHistoryPoint(1_780_086_400_000L, BigDecimal.valueOf(101), BigDecimal.valueOf(123), BigDecimal.valueOf(100), BigDecimal.valueOf(120), 2_000_000L)
        ));

        mvc.perform(post("/api/strategies/" + strategyId + "/transactions")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "ticker": "NVDA",
                      "side": "buy",
                      "quantity": 10,
                      "decisionId": null,
                      "executedAt": "2026-05-01T08:00:00Z"
                    }
                    """))
            .andExpect(status().isCreated());

        mvc.perform(get("/api/strategies/" + strategyId + "/history")
                .header("Authorization", "Bearer alice")
                .queryParam("range", "1mo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.series[0].symbol").value("NVDA"))
            .andExpect(jsonPath("$.performance.length()").value(2))
            .andExpect(jsonPath("$.performance[0].value").value(99800.00))
            .andExpect(jsonPath("$.performance[1].value").value(100000.00));
    }

    private long createStrategy(String ownerId, String name, String visibility) throws Exception {
        String response = mvc.perform(post("/api/strategies")
                .header("Authorization", "Bearer " + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "%s",
                      "description": "Virtual strategy for process tracking.",
                      "startingCapital": 100000,
                      "visibility": "%s"
                    }
                    """.formatted(name, visibility)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void addSymbol(String ownerId, long strategyId, String symbol, String visibility) throws Exception {
        mvc.perform(post("/api/strategies/" + strategyId + "/symbols")
                .header("Authorization", "Bearer " + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "symbol": "%s",
                      "note": "Research surface",
                      "tags": ["research"],
                      "visibility": "%s"
                    }
                    """.formatted(symbol, visibility)))
            .andExpect(status().isCreated());
    }

    private long createDecision(String ownerId, String ticker) throws Exception {
        String response = mvc.perform(post("/api/decisions")
                .header("Authorization", "Bearer " + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "ticker": "%s",
                      "decisionType": "watch",
                      "title": "%s thesis",
                      "thesis": "Decision linked to strategy transaction.",
                      "evidence": ["Evidence"],
                      "riskFactors": ["Risk"],
                      "confidence": 8,
                      "timeHorizon": "6 months",
                      "exitCriteria": ["Exit"],
                      "visibility": "private"
                    }
                    """.formatted(ticker, ticker)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private String transactionBody(String ticker, long decisionId) {
        return """
            {
              "ticker": "%s",
              "side": "buy",
              "quantity": 10,
              "decisionId": %d,
              "executedAt": "2026-06-05T08:00:00Z"
            }
            """.formatted(ticker, decisionId);
    }

    private void mockRegularQuote(String symbol, double price) {
        when(marketDataService.quote(eq(symbol))).thenReturn(Optional.of(new MarketDataService.MarketQuote(
            symbol,
            BigDecimal.valueOf(price),
            BigDecimal.valueOf(5),
            BigDecimal.valueOf(3.45),
            1L,
            "REGULAR",
            1000L
        )));
    }

    private void mockClosedQuote(String symbol, double price) {
        when(marketDataService.quote(eq(symbol))).thenReturn(Optional.of(new MarketDataService.MarketQuote(
            symbol,
            BigDecimal.valueOf(price),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            1L,
            "POST",
            1000L
        )));
    }

    @Test
    void technicalAnalysisReturnsIndicatorsAndRecommendation() throws Exception {
        long strategyId = createStrategy("alice", "Analysis Strategy", "private");
        addSymbol("alice", strategyId, "AAPL", "private");

        List<MarketDataService.MarketHistoryPoint> history = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            long timestamp = 1_780_000_000_000L + (i * 86_400_000L);
            BigDecimal close = BigDecimal.valueOf(150 + Math.sin(i / 20.0) * 10);
            history.add(new MarketDataService.MarketHistoryPoint(
                timestamp,
                close,
                close.add(BigDecimal.valueOf(1)),
                close.subtract(BigDecimal.valueOf(1)),
                close,
                1_000_000L
            ));
        }

        when(marketDataService.history(eq("AAPL"), anyString())).thenReturn(history);

        mvc.perform(get("/api/strategies/" + strategyId + "/analysis/AAPL")
                .header("Authorization", "Bearer alice")
                .queryParam("range", "1y"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.indicators").exists())
            .andExpect(jsonPath("$.recommendation").exists())
            .andExpect(jsonPath("$.recommendation.label").isString())
            .andExpect(jsonPath("$.recommendation.confidence").isString())
            .andExpect(jsonPath("$.recommendation.sampleSize").isNumber());
    }
}
