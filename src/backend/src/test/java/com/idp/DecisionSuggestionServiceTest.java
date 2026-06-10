package com.idp;

// Traces: FEAT-decision-journal-001, CON-investment-non-advice-001

import com.idp.service.DecisionSuggestionService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionSuggestionServiceTest {

    private final DecisionSuggestionService service = new DecisionSuggestionService();

    @Test
    void suggestionsCoverResearchRiskAndPortfolioRationales() {
        assertThat(service.getThesisSuggestions())
            .contains(
                "Business quality or durable competitive advantage",
                "Portfolio hedge or diversification role"
            );
        assertThat(service.getEvidenceSuggestions())
            .contains(
                "Revenue or free cash flow growth improving",
                "Balance sheet strength supports downside risk"
            );
        assertThat(service.getRisksSuggestions())
            .contains(
                "Execution risk or catalyst delay",
                "Position sizing or concentration risk"
            );
    }
}
