package com.idp;

// Traces: US-001, CONR-decision-record-api-001, INV-decision-record-integrity-001, CON-private-research-default-001

import com.idp.repository.DecisionRecordRepository;
import com.idp.repository.DecisionRevisionRepository;
import com.idp.repository.DecisionReviewRepository;
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
class DecisionRecordCreateTest {

    @Autowired MockMvc mvc;
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
    void createDecision_returns201WithPrivateDraftAndRevision() throws Exception {
        mvc.perform(post("/api/decisions")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "ticker": "NVDA",
                      "decisionType": "buy",
                      "title": "NVDA AI infrastructure thesis",
                      "thesis": "AI infrastructure demand can support revenue growth.",
                      "evidence": ["Data center revenue growth", "GPU supply constraints easing"],
                      "riskFactors": ["Valuation compression", "Export controls"],
                      "confidence": 8,
                      "timeHorizon": "12 months",
                      "exitCriteria": ["Revenue growth slows", "Thesis invalidated"],
                      "visibility": "private"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.status").value("draft"))
            .andExpect(jsonPath("$.createdAt").exists());

        var records = decisionRecordRepository.findAll();
        assertThat(records).hasSize(1);
        assertThat(records.getFirst().getOwnerId()).isEqualTo("alice");
        assertThat(records.getFirst().getVisibility().getValue()).isEqualTo("private");
        assertThat(decisionRevisionRepository.findByDecisionRecordIdOrderByRevisionNumber(records.getFirst().getId()))
            .hasSize(1)
            .first()
            .satisfies(revision -> {
                assertThat(revision.getRevisionNumber()).isEqualTo(1);
                assertThat(revision.getThesis()).contains("AI infrastructure");
            });
    }

    @Test
    void createDecisionWithoutAuthentication_returns401() throws Exception {
        mvc.perform(post("/api/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createDecisionWithMissingRequiredFields_returns400() throws Exception {
        mvc.perform(post("/api/decisions")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ticker\":\"NVDA\",\"confidence\":8,\"visibility\":\"private\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createDecisionWithInvalidConfidence_returns400() throws Exception {
        mvc.perform(post("/api/decisions")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody().replace("\"confidence\": 8", "\"confidence\": 11")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createDecisionWithPublicVisibility_returns400() throws Exception {
        mvc.perform(post("/api/decisions")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody().replace("\"visibility\": \"private\"", "\"visibility\": \"public\"")))
            .andExpect(status().isBadRequest());
    }

    private String validBody() {
        return """
            {
              "ticker": "AAPL",
              "decisionType": "watch",
              "title": "AAPL services margin watch",
              "thesis": "Services margin may support earnings quality.",
              "evidence": ["Services revenue growth"],
              "riskFactors": ["Hardware cycle weakness"],
              "confidence": 8,
              "timeHorizon": "6 months",
              "exitCriteria": ["Services growth weakens"],
              "visibility": "private"
            }
            """;
    }
}
