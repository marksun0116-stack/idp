package com.idp.service;

import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Provides predefined suggestion options for decision capture.
 * These suggestions help users quickly articulate their investment thesis, evidence, and risks.
 */
@Service
public class DecisionSuggestionService {

    public List<String> getThesisSuggestions() {
        return List.of(
            "Stock is undervalued (P/E or price/book below peers)",
            "Technical breakout signal (price breaks resistance)",
            "Momentum play (trend continuation)",
            "Mean reversion (oversold indicator like RSI < 30)",
            "Matches my investment strategy or watchlist criteria",
            "Business quality or durable competitive advantage",
            "Turnaround or margin improvement thesis",
            "Dividend or income-focused position",
            "Portfolio hedge or diversification role"
        );
    }

    public List<String> getEvidenceSuggestions() {
        return List.of(
            "P/E ratio below sector average",
            "RSI shows oversold conditions (< 30)",
            "Price above 50-day moving average",
            "Recent earnings beat or positive catalyst",
            "Sector/industry showing relative strength",
            "Revenue or free cash flow growth improving",
            "Margin expansion or cost discipline visible",
            "Balance sheet strength supports downside risk",
            "Management guidance or analyst estimates improving"
        );
    }

    public List<String> getRisksSuggestions() {
        return List.of(
            "Market downturn or sector correction",
            "Company earnings miss or guidance cut",
            "Sector rotation or fund flows shifting",
            "Valuation multiple compression",
            "Geopolitical or macro risk event",
            "Execution risk or catalyst delay",
            "Debt, refinancing, or liquidity risk",
            "Regulatory or legal risk",
            "Position sizing or concentration risk"
        );
    }
}
