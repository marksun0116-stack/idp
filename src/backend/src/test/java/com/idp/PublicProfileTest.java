package com.idp;

// Traces: US-011, CONR-public-profile-api-001, INV-private-data-visibility-001

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idp.repository.DecisionRecordRepository;
import com.idp.repository.DecisionReviewRepository;
import com.idp.repository.DecisionRevisionRepository;
import com.idp.repository.PublicProfileRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PublicProfileTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PublicProfileRepository publicProfileRepository;
    @Autowired StrategyPortfolioRepository strategyPortfolioRepository;
    @Autowired StrategyTrackedSymbolRepository trackedSymbolRepository;
    @Autowired StrategyTransactionRepository transactionRepository;
    @Autowired DecisionRecordRepository decisionRecordRepository;
    @Autowired DecisionRevisionRepository decisionRevisionRepository;
    @Autowired DecisionReviewRepository decisionReviewRepository;

    @BeforeEach
    void cleanUp() {
        publicProfileRepository.deleteAll();
        transactionRepository.deleteAll();
        trackedSymbolRepository.deleteAll();
        strategyPortfolioRepository.deleteAll();
        decisionReviewRepository.deleteAll();
        decisionRevisionRepository.deleteAll();
        decisionRecordRepository.deleteAll();
    }

    @Test
    void upsertProfilePublishesApprovedMetricsAndPublicStrategies() throws Exception {
        createDecision("alice", "NVDA");
        long publicStrategyId = createStrategy("alice", "Published AI Strategy", "public");
        addSymbol("alice", publicStrategyId, "NVDA", "public");

        mvc.perform(put("/api/profile/public")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "handle": "Alice_Research",
                      "displayName": "Alice Research",
                      "bio": "Structured process, public strategies only.",
                      "publishedMetricIds": ["dqs", "researchDiscipline", "riskManagement", "unknownMetric"]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.handle").value("alice_research"));

        mvc.perform(get("/api/profile/public").header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.handle").value("alice_research"))
            .andExpect(jsonPath("$.reputation.decisionQualityScore").isNumber())
            .andExpect(jsonPath("$.reputation.researchDiscipline").isNumber())
            .andExpect(jsonPath("$.reputation.riskManagement").isNumber())
            .andExpect(jsonPath("$.reputation.strategyConsistency").doesNotExist())
            .andExpect(jsonPath("$.publishedStrategies.length()").value(1));

        mvc.perform(get("/api/public/investors/alice_research"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.displayName").value("Alice Research"))
            .andExpect(jsonPath("$.publishedStrategies[0].id").value(publicStrategyId))
            .andExpect(jsonPath("$.publishedStrategies[0].publicTrackedSymbolCount").value(1));
    }

    @Test
    void publicProfileDoesNotLeakPrivateStrategiesOrPrivateTrackedSymbols() throws Exception {
        long publicStrategyId = createStrategy("alice", "Public Strategy", "public");
        long privateStrategyId = createStrategy("alice", "Private Watchlist", "private");
        addSymbol("alice", publicStrategyId, "NVDA", "public");
        addSymbol("alice", publicStrategyId, "TSLA", "private");
        addSymbol("alice", privateStrategyId, "MSFT", "private");
        publishProfile("alice", "alice");

        mvc.perform(get("/api/public/investors/alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.publishedStrategies.length()").value(1))
            .andExpect(jsonPath("$.publishedStrategies[0].id").value(publicStrategyId))
            .andExpect(jsonPath("$.publishedStrategies[0].name").value("Public Strategy"))
            .andExpect(jsonPath("$.publishedStrategies[0].publicTrackedSymbolCount").value(1));
    }

    @Test
    void duplicateHandleIsRejectedAndUnknownHandleIsNotFound() throws Exception {
        publishProfile("alice", "shared");

        mvc.perform(put("/api/profile/public")
                .header("Authorization", "Bearer bob")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "handle": "shared",
                      "displayName": "Bob",
                      "bio": null,
                      "publishedMetricIds": []
                    }
                    """))
            .andExpect(status().isConflict());

        mvc.perform(get("/api/public/investors/missing"))
            .andExpect(status().isNotFound());
    }

    private void publishProfile(String ownerId, String handle) throws Exception {
        mvc.perform(put("/api/profile/public")
                .header("Authorization", "Bearer " + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "handle": "%s",
                      "displayName": "%s",
                      "bio": "Public profile",
                      "publishedMetricIds": ["dqs", "strategyConsistency"]
                    }
                    """.formatted(handle, ownerId)))
            .andExpect(status().isOk());
    }

    private long createStrategy(String ownerId, String name, String visibility) throws Exception {
        String response = mvc.perform(post("/api/strategies")
                .header("Authorization", "Bearer " + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "%s",
                      "description": "Virtual strategy for public profile tests.",
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
                      "note": "Visibility test",
                      "tags": ["profile"],
                      "visibility": "%s"
                    }
                    """.formatted(symbol, visibility)))
            .andExpect(status().isCreated());
    }

    private void createDecision(String ownerId, String ticker) throws Exception {
        mvc.perform(post("/api/decisions")
                .header("Authorization", "Bearer " + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "ticker": "%s",
                      "decisionType": "watch",
                      "title": "%s profile decision",
                      "thesis": "Profile metric seed.",
                      "evidence": ["Evidence one", "Evidence two", "Evidence three"],
                      "riskFactors": ["Risk one", "Risk two"],
                      "confidence": 8,
                      "timeHorizon": "6 months",
                      "exitCriteria": ["Exit one", "Exit two"],
                      "visibility": "private"
                    }
                    """.formatted(ticker, ticker)))
            .andExpect(status().isCreated());
    }
}
