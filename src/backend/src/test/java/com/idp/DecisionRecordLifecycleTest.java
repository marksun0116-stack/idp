package com.idp;

// Traces: US-002, CONR-decision-record-api-001, INV-private-data-visibility-001, INV-decision-record-integrity-001

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DecisionRecordLifecycleTest {

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
    void listAndGetDecision_returnsOnlyAuthenticatedOwnersRecords() throws Exception {
        long aliceDecisionId = createDecision("alice", "NVDA");
        createDecision("bob", "MSFT");

        mvc.perform(get("/api/decisions").header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.decisions.length()").value(1))
            .andExpect(jsonPath("$.decisions[0].id").value(aliceDecisionId))
            .andExpect(jsonPath("$.decisions[0].ticker").value("NVDA"));

        mvc.perform(get("/api/decisions/" + aliceDecisionId).header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(aliceDecisionId))
            .andExpect(jsonPath("$.visibility").value("private"))
            .andExpect(jsonPath("$.thesis").value("NVDA thesis"));

        mvc.perform(get("/api/decisions/" + aliceDecisionId).header("Authorization", "Bearer bob"))
            .andExpect(status().isNotFound());
    }

    @Test
    void listDecision_canFilterByStatusAndTicker() throws Exception {
        long nvdaDecisionId = createDecision("alice", "NVDA");
        createDecision("alice", "AAPL");
        transition("alice", nvdaDecisionId, "active");

        mvc.perform(get("/api/decisions")
                .header("Authorization", "Bearer alice")
                .queryParam("status", "active")
                .queryParam("ticker", "nvda"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.decisions.length()").value(1))
            .andExpect(jsonPath("$.decisions[0].id").value(nvdaDecisionId))
            .andExpect(jsonPath("$.decisions[0].status").value("active"));
    }

    @Test
    void updateDecision_updatesMutableFieldsAndCreatesRevision() throws Exception {
        long decisionId = createDecision("alice", "NVDA");

        mvc.perform(put("/api/decisions/" + decisionId)
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Updated NVDA thesis",
                      "thesis": "Updated thesis with changed evidence.",
                      "evidence": ["Updated revenue signal"],
                      "riskFactors": ["Updated valuation risk"],
                      "confidence": 7,
                      "timeHorizon": "9 months",
                      "exitCriteria": ["Updated exit trigger"]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(decisionId))
            .andExpect(jsonPath("$.revision").value(2));

        mvc.perform(get("/api/decisions/" + decisionId).header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated NVDA thesis"))
            .andExpect(jsonPath("$.confidence").value(7));

        var revisions = decisionRevisionRepository.findByDecisionRecordIdOrderByRevisionNumber(decisionId);
        assertThat(revisions).hasSize(2);
        assertThat(revisions.getFirst().getTitle()).isEqualTo("NVDA decision");
        assertThat(revisions.get(1).getTitle()).isEqualTo("Updated NVDA thesis");
    }

    @Test
    void transitionDecision_closesAndArchivesOwnedDecision() throws Exception {
        long decisionId = createDecision("alice", "NVDA");

        transition("alice", decisionId, "active")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("active"));

        transition("alice", decisionId, "closed")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("closed"));

        transition("alice", decisionId, "archived")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("archived"));
    }

    @Test
    void updateClosedDecision_returns409AndDoesNotAddRevision() throws Exception {
        long decisionId = createDecision("alice", "NVDA");
        transition("alice", decisionId, "active");
        transition("alice", decisionId, "closed");

        mvc.perform(put("/api/decisions/" + decisionId)
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody()))
            .andExpect(status().isConflict());

        assertThat(decisionRevisionRepository.findByDecisionRecordIdOrderByRevisionNumber(decisionId)).hasSize(1);
    }

    @Test
    void invalidTransition_returns409() throws Exception {
        long decisionId = createDecision("alice", "NVDA");

        transition("alice", decisionId, "closed")
            .andExpect(status().isConflict());
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
        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions transition(String ownerId, long decisionId, String status) throws Exception {
        return mvc.perform(post("/api/decisions/" + decisionId + "/transition")
            .header("Authorization", "Bearer " + ownerId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "status": "%s",
                  "reason": "Lifecycle test transition"
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

    private String updateBody() {
        return """
            {
              "title": "Should not update",
              "thesis": "Should not update",
              "evidence": ["Evidence"],
              "riskFactors": ["Risk"],
              "confidence": 6,
              "timeHorizon": "6 months",
              "exitCriteria": ["Exit"]
            }
            """;
    }
}
