package com.idp;

// Traces: US-005, CONR-dqs-api-001, INV-dqs-explainability-001, CON-investment-non-advice-001

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idp.repository.DecisionRecordRepository;
import com.idp.repository.DecisionReviewRepository;
import com.idp.repository.DecisionRevisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AnalyticsDqsTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired DecisionRecordRepository decisionRecordRepository;
    @Autowired DecisionRevisionRepository decisionRevisionRepository;
    @Autowired DecisionReviewRepository decisionReviewRepository;

    @BeforeEach
    void cleanUp() {
        decisionReviewRepository.deleteAll();
        decisionRevisionRepository.deleteAll();
        decisionRecordRepository.deleteAll();
    }

    @Test
    void dqsReturnsExplainableWeightedScoreForOwner() throws Exception {
        long aliceDecisionId = createDecision("alice", "NVDA", 8, 3, 2, 2);
        activate("alice", aliceDecisionId);
        completeFirstReview("alice", aliceDecisionId);
        long bobDecisionId = createDecision("bob", "MSFT", 3, 1, 1, 1);
        activate("bob", bobDecisionId);

        mvc.perform(get("/api/analytics/dqs").header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.score", greaterThan(70)))
            .andExpect(jsonPath("$.trend").value(0))
            .andExpect(jsonPath("$.components.researchQuality.weight").value(0.25))
            .andExpect(jsonPath("$.components.decisionDiscipline.weight").value(0.25))
            .andExpect(jsonPath("$.components.riskManagement.weight").value(0.2))
            .andExpect(jsonPath("$.components.strategyConsistency.weight").value(0.15))
            .andExpect(jsonPath("$.components.outcomeQuality.weight").value(0.15))
            .andExpect(jsonPath("$.components.outcomeQuality.score", lessThanOrEqualTo(100)))
            .andExpect(jsonPath("$.drivers.length()", greaterThan(0)))
            .andExpect(jsonPath("$.drivers[0].relatedDecisionIds[0]").value(aliceDecisionId));
    }

    @Test
    void dqsEmptyStateReturnsNeutralScores() throws Exception {
        mvc.perform(get("/api/analytics/dqs").header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.score").value(50))
            .andExpect(jsonPath("$.components.outcomeQuality.weight").value(0.15))
            .andExpect(jsonPath("$.drivers[0].label").value("Start by recording structured decisions with evidence, risks, and exit criteria."));
    }

    private long createDecision(String ownerId, String ticker, int confidence, int evidenceCount, int riskCount, int exitCount) throws Exception {
        String response = mvc.perform(post("/api/decisions")
                .header("Authorization", "Bearer " + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody(ticker, confidence, evidenceCount, riskCount, exitCount)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void activate(String ownerId, long decisionId) throws Exception {
        mvc.perform(post("/api/decisions/" + decisionId + "/transition")
                .header("Authorization", "Bearer " + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "active",
                      "reason": "Analytics test transition"
                    }
                    """))
            .andExpect(status().isOk());
    }

    private void completeFirstReview(String ownerId, long decisionId) throws Exception {
        long reviewId = decisionReviewRepository.findByDecisionRecordIdOrderByDueDate(decisionId).getFirst().getId();
        mvc.perform(post("/api/reviews/" + reviewId + "/complete")
                .header("Authorization", "Bearer " + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "outcomeSummary": "Thesis and risk notes were useful for review.",
                      "thesisAccuracy": 8,
                      "riskAssessmentAccuracy": 8,
                      "lessonsLearned": ["Keep comparing expected risks to actual outcomes"],
                      "nextAction": "Continue monitoring process"
                    }
                    """))
            .andExpect(status().isOk());
    }

    private String createBody(String ticker, int confidence, int evidenceCount, int riskCount, int exitCount) {
        return """
            {
              "ticker": "%s",
              "decisionType": "watch",
              "title": "%s decision",
              "thesis": "%s thesis",
              "evidence": %s,
              "riskFactors": %s,
              "confidence": %d,
              "timeHorizon": "6 months",
              "exitCriteria": %s,
              "visibility": "private"
            }
            """.formatted(ticker, ticker, ticker, array("Evidence", evidenceCount), array("Risk", riskCount), confidence, array("Exit", exitCount));
    }

    private String array(String prefix, int count) {
        if (count == 0) {
            return "[]";
        }
        return "[" + java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(index -> "\"" + prefix + " " + index + "\"")
            .collect(java.util.stream.Collectors.joining(", ")) + "]";
    }
}
