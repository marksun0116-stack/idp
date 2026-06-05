package com.idp;

// Traces: US-004, CONR-decision-review-api-001, INV-review-schedule-integrity-001

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idp.model.ReviewStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DecisionReviewCompletionTest {

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
    void completeReview_recordsLearningFields() throws Exception {
        long reviewId = createActiveDecisionAndFirstReview("alice", "NVDA");

        mvc.perform(post("/api/reviews/" + reviewId + "/complete")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCompletionBody()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(reviewId))
            .andExpect(jsonPath("$.status").value("completed"))
            .andExpect(jsonPath("$.completedAt").exists());

        var review = decisionReviewRepository.findById(reviewId).orElseThrow();
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.COMPLETED);
        assertThat(review.getOutcomeSummary()).contains("Thesis played out");
        assertThat(review.getThesisAccuracy()).isEqualTo(8);
        assertThat(review.getRiskAssessmentAccuracy()).isEqualTo(7);
        assertThat(review.getLessonsLearned()).containsExactly("Track valuation risk earlier");
        assertThat(review.getNextAction()).isEqualTo("Keep active");
        assertThat(review.getDecisionRecord().getTicker()).isEqualTo("NVDA");
    }

    @Test
    void completeReviewForAnotherOwner_returns404() throws Exception {
        long reviewId = createActiveDecisionAndFirstReview("alice", "NVDA");

        mvc.perform(post("/api/reviews/" + reviewId + "/complete")
                .header("Authorization", "Bearer bob")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCompletionBody()))
            .andExpect(status().isNotFound());
    }

    @Test
    void completeReviewWithInvalidRatings_returns400() throws Exception {
        long reviewId = createActiveDecisionAndFirstReview("alice", "NVDA");

        mvc.perform(post("/api/reviews/" + reviewId + "/complete")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCompletionBody().replace("\"thesisAccuracy\": 8", "\"thesisAccuracy\": 11")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void completeAlreadyCompletedReview_returns409() throws Exception {
        long reviewId = createActiveDecisionAndFirstReview("alice", "NVDA");

        mvc.perform(post("/api/reviews/" + reviewId + "/complete")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCompletionBody()))
            .andExpect(status().isOk());

        mvc.perform(post("/api/reviews/" + reviewId + "/complete")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCompletionBody()))
            .andExpect(status().isConflict());
    }

    private long createActiveDecisionAndFirstReview(String ownerId, String ticker) throws Exception {
        long decisionId = createDecision(ownerId, ticker);
        mvc.perform(post("/api/decisions/" + decisionId + "/transition")
                .header("Authorization", "Bearer " + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "active",
                      "reason": "Completion test transition"
                    }
                    """))
            .andExpect(status().isOk());
        return decisionReviewRepository.findByDecisionRecordIdOrderByDueDate(decisionId).getFirst().getId();
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

    private String validCompletionBody() {
        return """
            {
              "outcomeSummary": "Thesis played out better than expected.",
              "thesisAccuracy": 8,
              "riskAssessmentAccuracy": 7,
              "lessonsLearned": ["Track valuation risk earlier"],
              "nextAction": "Keep active"
            }
            """;
    }
}
