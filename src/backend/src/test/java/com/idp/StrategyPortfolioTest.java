package com.idp;

// Traces: US-007, US-008, US-009, US-010, CONR-strategy-portfolio-api-001, INV-strategy-market-data-scope-001

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idp.repository.DecisionRecordRepository;
import com.idp.repository.DecisionReviewRepository;
import com.idp.repository.DecisionRevisionRepository;
import com.idp.repository.StrategyPortfolioRepository;
import com.idp.repository.StrategyTrackedSymbolRepository;
import com.idp.repository.StrategyTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @BeforeEach
    void cleanUp() {
        transactionRepository.deleteAll();
        trackedSymbolRepository.deleteAll();
        strategyPortfolioRepository.deleteAll();
        decisionReviewRepository.deleteAll();
        decisionRevisionRepository.deleteAll();
        decisionRecordRepository.deleteAll();
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
            .andExpect(jsonPath("$.trackingStatus").value("tracked"));

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
    void appendTransactionCreatesImmutableEventAndValidatesDecisionOwner() throws Exception {
        long strategyId = createStrategy("alice", "Public Strategy", "public");
        long decisionId = createDecision("alice", "NVDA");
        long bobDecisionId = createDecision("bob", "MSFT");

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
              "price": 125.50,
              "decisionId": %d,
              "executedAt": "2026-06-05T08:00:00Z"
            }
            """.formatted(ticker, decisionId);
    }
}
