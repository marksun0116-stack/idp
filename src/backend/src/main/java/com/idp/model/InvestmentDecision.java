package com.idp.model;

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
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Investment Decision Journal entry.
 * Captures buy/sell transaction details with thesis, evidence, risks, and exit criteria.
 * Once closed, becomes read-only for historical accuracy.
 */
@Entity
@Table(name = "investment_decisions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "symbol", "transaction_date", "action", "quantity", "price"})
})
public class InvestmentDecision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionType action; // BUY or SELL

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false, length = 500)
    private String title; // Auto-generated: "Buy 200 shares of AAPL at $150"

    @Column(length = 2000)
    private String thesis;

    @Column(length = 2000)
    private String evidence;

    @Column(length = 2000)
    private String risks;

    @Column(length = 2000)
    private String comments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionStatus status; // DRAFT, ACTIVE, CLOSED

    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InvestmentDecisionAlert> alerts = new ArrayList<>();

    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InvestmentDecisionEdit> editHistory = new ArrayList<>();

    @Column(precision = 10, scale = 2)
    private BigDecimal exitPrice; // Final price when closed

    @Column(precision = 10, scale = 2)
    private BigDecimal exitPnl; // Final P/L when closed

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private Instant closedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = DecisionStatus.ACTIVE;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public DecisionType getAction() {
        return action;
    }

    public void setAction(DecisionType action) {
        this.action = action;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThesis() {
        return thesis;
    }

    public void setThesis(String thesis) {
        this.thesis = thesis;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public String getRisks() {
        return risks;
    }

    public void setRisks(String risks) {
        this.risks = risks;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public DecisionStatus getStatus() {
        return status;
    }

    public void setStatus(DecisionStatus status) {
        this.status = status;
    }

    public List<InvestmentDecisionAlert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<InvestmentDecisionAlert> alerts) {
        this.alerts = alerts;
    }

    public List<InvestmentDecisionEdit> getEditHistory() {
        return editHistory;
    }

    public void setEditHistory(List<InvestmentDecisionEdit> editHistory) {
        this.editHistory = editHistory;
    }

    public BigDecimal getExitPrice() {
        return exitPrice;
    }

    public void setExitPrice(BigDecimal exitPrice) {
        this.exitPrice = exitPrice;
    }

    public BigDecimal getExitPnl() {
        return exitPnl;
    }

    public void setExitPnl(BigDecimal exitPnl) {
        this.exitPnl = exitPnl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public boolean isClosed() {
        return status == DecisionStatus.CLOSED;
    }
}
