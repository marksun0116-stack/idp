package com.idp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Exit criteria alert for an InvestmentDecision.
 * Tracks alert conditions (price targets, P/L ratios) and when they trigger.
 * Alerts are auto-created from exit criteria and shown in the decision journal.
 */
@Entity
@Table(name = "investment_decision_alerts")
public class InvestmentDecisionAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "decision_id", nullable = false)
    private InvestmentDecision decision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertConditionType conditionType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal conditionValue; // $165 or 10% or 0.10

    @Column(precision = 10, scale = 2)
    private BigDecimal triggeredPrice; // Price when alert triggered

    private Instant triggeredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status; // PENDING, TRIGGERED, CLOSED

    private String description; // Human-readable: "Price ≥ $165 (take profit)"

    // Constructors
    public InvestmentDecisionAlert() {
    }

    public InvestmentDecisionAlert(InvestmentDecision decision, AlertConditionType conditionType,
                                   BigDecimal conditionValue, String description) {
        this.decision = decision;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.description = description;
        this.status = AlertStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public InvestmentDecision getDecision() {
        return decision;
    }

    public void setDecision(InvestmentDecision decision) {
        this.decision = decision;
    }

    public AlertConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(AlertConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public BigDecimal getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(BigDecimal conditionValue) {
        this.conditionValue = conditionValue;
    }

    public BigDecimal getTriggeredPrice() {
        return triggeredPrice;
    }

    public void setTriggeredPrice(BigDecimal triggeredPrice) {
        this.triggeredPrice = triggeredPrice;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(Instant triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isTriggered() {
        return status == AlertStatus.TRIGGERED;
    }

    // Enums
    public enum AlertConditionType {
        PRICE_ABOVE("price_above"),
        PRICE_BELOW("price_below"),
        PRICE_AT("price_at"),
        PNL_ABOVE("pnl_above"),
        PNL_BELOW("pnl_below"),
        REVIEW_AFTER_DAYS("review_after_days"),
        VALUATION_ABOVE("valuation_above"),
        VALUATION_BELOW("valuation_below");

        private final String value;

        AlertConditionType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum AlertStatus {
        PENDING("pending"),
        TRIGGERED("triggered"),
        CLOSED("closed");

        private final String value;

        AlertStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
