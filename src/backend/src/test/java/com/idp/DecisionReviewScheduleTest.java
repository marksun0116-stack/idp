package com.idp;

// Traces: US-003, CONR-decision-review-api-001, CON-decision-review-cadence-001, INV-review-schedule-integrity-001

import com.fasterxml.jackson.databind.JsonNode;
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

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DecisionReviewScheduleTest {

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
    void activeTransitionCreatesReviewMilestones() throws Exception {
        long decisionId = createDecision("alice", "NVDA");
        LocalDate activationDate = LocalDate.now(ZoneOffset.UTC);

        transition("alice", decisionId, "active")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("active"));

        var reviews = decisionReviewRepository.findByDecisionRecordIdOrderByDueDate(decisionId);
        assertThat(reviews).hasSize(4);
        assertThat(reviews).extracting(review -> review.getReviewType().getValue())
            .containsExactly("30d", "90d", "180d", "1y");
        assertThat(reviews).extracting("dueDate").containsExactly(
            activationDate.plusDays(30),
            activationDate.plusDays(90),
            activationDate.plusDays(180),
            activationDate.plusYears(1)
        );

        mvc.perform(get("/api/reviews")
                .header("Authorization", "Bearer alice")
                .queryParam("decisionId", Long.toString(decisionId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reviews.length()").value(4))
            .andExpect(jsonPath("$.reviews[0].reviewType").value("30d"))
            .andExpect(jsonPath("$.reviews[0].status").value("pending"));
    }

    @Test
    void activeTransitionIsIdempotentForReviewSchedule() throws Exception {
        long decisionId = createDecision("alice", "NVDA");

        transition("alice", decisionId, "active").andExpect(status().isOk());
        transition("alice", decisionId, "active").andExpect(status().isOk());

        assertThat(decisionReviewRepository.findByDecisionRecordIdOrderByDueDate(decisionId)).hasSize(4);
    }

    @Test
    void reviewListReturnsOnlyAuthenticatedOwnersReviews() throws Exception {
        long aliceDecisionId = createDecision("alice", "NVDA");
        long bobDecisionId = createDecision("bob", "MSFT");
        transition("alice", aliceDecisionId, "active").andExpect(status().isOk());
        transition("bob", bobDecisionId, "active").andExpect(status().isOk());

        mvc.perform(get("/api/reviews").header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reviews.length()").value(4));

        String response = mvc.perform(get("/api/reviews").header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        JsonNode reviews = objectMapper.readTree(response).get("reviews");
        assertThat(reviews).hasSize(4);
        assertThat(reviewDecisionIds(reviews)).containsOnly(aliceDecisionId);
    }

    private Set<Long> reviewDecisionIds(JsonNode reviews) {
        Set<Long> decisionIds = new HashSet<>();
        reviews.forEach(review -> decisionIds.add(review.get("decisionId").asLong()));
        return decisionIds;
    }

    private long createDecision(String ownerId, String ticker) throws Exception {
        String response = mvc.perform(post("/api/decisions")
                .header("Authorization", "Bearer " + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody(ticker)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions transition(String ownerId, long decisionId, String status) throws Exception {
        return mvc.perform(post("/api/decisions/" + decisionId + "/transition")
            .header("Authorization", "Bearer " + ownerId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "status": "%s",
                  "reason": "Schedule test transition"
                }
                """.formatted(status)));
    }

    private String createBody(String ticker) {
        return """
            {
              "ticker": "%s",
              "decisionType": "watch",
              "title": "%s decision",
              "thesis": "%s thesis",
              "evidence": ["Evidence"],
              "riskFactors": ["Risk"],
              "confidence": 8,
              "timeHorizon": "6 months",
              "exitCriteria": ["Exit"],
              "visibility": "private"
            }
            """.formatted(ticker, ticker, ticker);
    }
}
