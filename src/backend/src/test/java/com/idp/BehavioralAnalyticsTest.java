package com.idp;

// Traces: US-006, CONR-behavioral-analytics-api-001, CON-private-research-default-001, CON-investment-non-advice-001

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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BehavioralAnalyticsTest {

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
    void behaviorReturnsPrivateCoachingInsights() throws Exception {
        long decisionId = createDecision("alice", "NVDA", 8, 3, 2, 2);
        activate("alice", decisionId);
        createDecision("bob", "MSFT", 3, 1, 1, 1);

        mvc.perform(get("/api/analytics/behavior").header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.behavioralScore", greaterThanOrEqualTo(70)))
            .andExpect(jsonPath("$.researchDisciplineScore").value(100))
            .andExpect(jsonPath("$.riskDisciplineScore").value(100))
            .andExpect(jsonPath("$.insights.length()", greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.insights[0].relatedDecisionIds[0]").value(decisionId))
            .andExpect(jsonPath("$.insights[0].detail", not(containsString("buy"))))
            .andExpect(jsonPath("$.insights[0].detail", not(containsString("sell"))));
    }

    @Test
    void behaviorEmptyStateReturnsNeutralCoaching() throws Exception {
        mvc.perform(get("/api/analytics/behavior").header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.behavioralScore").value(50))
            .andExpect(jsonPath("$.insights[0].type").value("neutral"));
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
                      "reason": "Behavior analytics test transition"
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
