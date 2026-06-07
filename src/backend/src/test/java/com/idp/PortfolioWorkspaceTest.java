package com.idp;

// Traces: RR-009, US-016, US-017, US-018, CONR-portfolio-api-001, INV-portfolio-ownership-001, INV-holding-cost-basis-001, INV-manual-price-001

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idp.repository.HoldingRepository;
import com.idp.repository.InvestmentAccountRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
class PortfolioWorkspaceTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired InvestmentAccountRepository accountRepository;
    @Autowired HoldingRepository holdingRepository;

    @MockBean MarketDataService marketDataService;

    @BeforeEach
    void cleanUp() {
        holdingRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void accountLifecycleIsOwnerScopedAndRejectsDuplicateNames() throws Exception {
        long aliceAccountId = createAccount("alice", "Core Brokerage", "BROKERAGE");
        createAccount("bob", "Core Brokerage", "BROKERAGE");

        mvc.perform(get("/api/portfolio/summary").header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accounts.length()").value(1))
            .andExpect(jsonPath("$.accounts[0].id").value(aliceAccountId))
            .andExpect(jsonPath("$.accounts[0].name").value("Core Brokerage"));

        mvc.perform(post("/api/portfolio/accounts")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "Core Brokerage", "accountType": "IRA" }
                    """))
            .andExpect(status().isConflict());

        mvc.perform(delete("/api/portfolio/accounts/" + aliceAccountId).header("Authorization", "Bearer bob"))
            .andExpect(status().isNotFound());

        mvc.perform(delete("/api/portfolio/accounts/" + aliceAccountId).header("Authorization", "Bearer alice"))
            .andExpect(status().isNoContent());
    }

    @Test
    void holdingCrudEnforcesOwnershipUniquenessAndCascadeDelete() throws Exception {
        long aliceAccountId = createAccount("alice", "Core Brokerage", "BROKERAGE");
        long holdingId = addHolding("alice", aliceAccountId, "nvda", "10", "100", null);

        mvc.perform(post("/api/portfolio/accounts/" + aliceAccountId + "/holdings")
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "symbol": "NVDA", "shares": 5, "costBasis": 110 }
                    """))
            .andExpect(status().isConflict());

        mvc.perform(put("/api/portfolio/accounts/" + aliceAccountId + "/holdings/" + holdingId)
                .header("Authorization", "Bearer bob")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "shares": 11, "costBasis": 101 }
                    """))
            .andExpect(status().isNotFound());

        mvc.perform(put("/api/portfolio/accounts/" + aliceAccountId + "/holdings/" + holdingId)
                .header("Authorization", "Bearer alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "shares": 12, "costBasis": null, "purchaseDate": "2026-06-01", "manualPrice": 125 }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value("NVDA"))
            .andExpect(jsonPath("$.shares").value(12))
            .andExpect(jsonPath("$.costBasis").doesNotExist())
            .andExpect(jsonPath("$.manualPrice").value(125))
            .andExpect(jsonPath("$.manualPriceActive").value(true));

        mvc.perform(delete("/api/portfolio/accounts/" + aliceAccountId).header("Authorization", "Bearer alice"))
            .andExpect(status().isNoContent());

        assertThat(holdingRepository.findAll()).isEmpty();
    }

    @Test
    void summaryUsesManualPriceBeforeMarketDataAndHandlesNullCostBasis() throws Exception {
        when(marketDataService.quote(eq("AAPL"))).thenReturn(Optional.of(new MarketDataService.MarketQuote(
            "AAPL",
            BigDecimal.valueOf(200),
            BigDecimal.valueOf(2),
            BigDecimal.valueOf(1),
            1L,
            "REGULAR",
            1000L
        )));
        long accountId = createAccount("alice", "Core Brokerage", "BROKERAGE");
        addHolding("alice", accountId, "AAPL", "3", "150", null);
        addHolding("alice", accountId, "CASHX", "2", null, "50");

        mvc.perform(get("/api/portfolio/summary").header("Authorization", "Bearer alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalValue").value(700.00))
            .andExpect(jsonPath("$.totalCost").value(450.00))
            .andExpect(jsonPath("$.totalGain").value(250.00))
            .andExpect(jsonPath("$.dailyGain").value(6.00))
            .andExpect(jsonPath("$.accounts[0].holdings.length()").value(2))
            .andExpect(jsonPath("$.accounts[0].holdings[0].symbol").value("AAPL"))
            .andExpect(jsonPath("$.accounts[0].holdings[0].value").value(600.00))
            .andExpect(jsonPath("$.accounts[0].holdings[0].dayGain").value(6.00))
            .andExpect(jsonPath("$.accounts[0].holdings[1].symbol").value("CASHX"))
            .andExpect(jsonPath("$.accounts[0].holdings[1].cost").doesNotExist())
            .andExpect(jsonPath("$.accounts[0].holdings[1].manualPriceActive").value(true))
            .andExpect(jsonPath("$.accounts[0].holdings[1].value").value(100.00));
    }

    private long createAccount(String ownerId, String name, String accountType) throws Exception {
        String response = mvc.perform(post("/api/portfolio/accounts")
                .header("Authorization", "Bearer " + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "%s", "accountType": "%s" }
                    """.formatted(name, accountType)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long addHolding(String ownerId, long accountId, String symbol, String shares, String costBasis, String manualPrice) throws Exception {
        String response = mvc.perform(post("/api/portfolio/accounts/" + accountId + "/holdings")
                .header("Authorization", "Bearer " + ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "symbol": "%s",
                      "shares": %s,
                      "costBasis": %s,
                      "manualPrice": %s
                    }
                    """.formatted(symbol, shares, costBasis == null ? "null" : costBasis, manualPrice == null ? "null" : manualPrice)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
