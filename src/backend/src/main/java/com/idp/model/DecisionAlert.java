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
 * Exit criteria alert for a Decision (Investment or Strategy).
 * Tracks alert conditions (price targets, P/L ratios) and when they trigger.
 */
@Entity
@Table(name = "decision_alerts")
public class DecisionAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertConditionType conditionType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal conditionValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal triggeredPrice;

    private Instant triggeredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    private String description;

    // Constructors
    public DecisionAlert() {
    }

    public DecisionAlert(Decision decision, AlertConditionType conditionType,
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

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
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

    public enum AlertConditionType {
        PRICE_ABOVE,
        PRICE_BELOW,
        PRICE_AT,
        PNL_ABOVE,
        PNL_BELOW,
        VALUATION_ABOVE,
        VALUATION_BELOW,
        REVIEW_AFTER_DAYS
    }

    public enum AlertStatus {
        PENDING,
        TRIGGERED,
        CLOSED
    }
}
