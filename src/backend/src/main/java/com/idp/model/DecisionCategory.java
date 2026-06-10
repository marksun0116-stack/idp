package com.idp.model;

/**
 * Distinguishes between investment decisions (transactions) and strategy decisions.
 */
public enum DecisionCategory {
    INVESTMENT, // Transaction-level buy/sell decisions
    STRATEGY    // Strategic investment thesis decisions
}
