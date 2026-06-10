package com.idp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified Decision entity that stores both Investment and Strategy decisions.
 * Uses decisionCategory to distinguish between types.
 */
@Entity
@Table(name = "decisions")
public class Decision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionCategory decisionCategory; // INVESTMENT or STRATEGY

    // Common fields
    @Column(nullable = false, length = 10)
    private String symbol; // Ticker symbol (e.g., AAPL)

    @Column(nullable = false, length = 500)
    private String title; // Decision title/summary

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionType decisionType; // BUY, SELL, WATCH, AVOID

    @Column(length = 2000)
    private String thesis; // Investment thesis (required for strategy, optional for investment)

    @Column(length = 2000)
    private String evidence; // Supporting evidence (comma or semicolon separated)

    @Column(length = 2000)
    private String risks; // Risk factors (comma or semicolon separated)

    @Column(length = 2000)
    private String comments; // Additional comments

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionStatus status; // ACTIVE, CLOSED, DRAFT

    // Investment Decision specific fields
    @Column(name = "action_type")
    @Enumerated(EnumType.STRING)
    private DecisionType action; // BUY or SELL (duplicate of decisionType for investment)

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;

    @Column(name = "exit_price", precision = 10, scale = 2)
    private BigDecimal exitPrice;

    @Column(name = "exit_pnl", precision = 10, scale = 2)
    private BigDecimal exitPnl;

    @Column(name = "close_reason", length = 100)
    private String closeReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private DecisionSource source; // MANUAL or AUTO for investments

    // Strategy Decision specific fields
    @Column(name = "confidence")
    private Integer confidence; // 1-10 confidence score

    @Column(name = "time_horizon", length = 100)
    private String timeHorizon; // e.g., "3 months", "1 year"

    @Column(name = "exit_criteria", length = 2000)
    private String exitCriteria; // Exit criteria (semicolon separated)

    // Relationships
    @JsonIgnore
    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DecisionAlert> alerts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DecisionEdit> editHistory = new ArrayList<>();

    // Audit fields
    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public DecisionCategory getDecisionCategory() { return decisionCategory; }
    public void setDecisionCategory(DecisionCategory decisionCategory) { this.decisionCategory = decisionCategory; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public DecisionType getDecisionType() { return decisionType; }
    public void setDecisionType(DecisionType decisionType) { this.decisionType = decisionType; }

    public String getThesis() { return thesis; }
    public void setThesis(String thesis) { this.thesis = thesis; }

    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }

    public String getRisks() { return risks; }
    public void setRisks(String risks) { this.risks = risks; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public DecisionStatus getStatus() { return status; }
    public void setStatus(DecisionStatus status) { this.status = status; }

    public DecisionType getAction() { return action; }
    public void setAction(DecisionType action) { this.action = action; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public BigDecimal getExitPrice() { return exitPrice; }
    public void setExitPrice(BigDecimal exitPrice) { this.exitPrice = exitPrice; }

    public BigDecimal getExitPnl() { return exitPnl; }
    public void setExitPnl(BigDecimal exitPnl) { this.exitPnl = exitPnl; }

    public String getCloseReason() { return closeReason; }
    public void setCloseReason(String closeReason) { this.closeReason = closeReason; }

    public DecisionSource getSource() { return source; }
    public void setSource(DecisionSource source) { this.source = source; }

    public Integer getConfidence() { return confidence; }
    public void setConfidence(Integer confidence) { this.confidence = confidence; }

    public String getTimeHorizon() { return timeHorizon; }
    public void setTimeHorizon(String timeHorizon) { this.timeHorizon = timeHorizon; }

    public String getExitCriteria() { return exitCriteria; }
    public void setExitCriteria(String exitCriteria) { this.exitCriteria = exitCriteria; }

    public List<DecisionAlert> getAlerts() { return alerts; }
    public void setAlerts(List<DecisionAlert> alerts) { this.alerts = alerts; }

    public List<DecisionEdit> getEditHistory() { return editHistory; }
    public void setEditHistory(List<DecisionEdit> editHistory) { this.editHistory = editHistory; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
