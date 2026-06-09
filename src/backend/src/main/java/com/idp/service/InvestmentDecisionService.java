package com.idp.service;

import com.idp.model.DecisionSource;
import com.idp.model.DecisionStatus;
import com.idp.model.DecisionType;
import com.idp.model.InvestmentDecision;
import com.idp.model.InvestmentDecisionAlert;
import com.idp.model.InvestmentDecisionAlert.AlertConditionType;
import com.idp.model.InvestmentDecisionAlert.AlertStatus;
import com.idp.model.InvestmentDecisionEdit;
import com.idp.repository.InvestmentDecisionAlertRepository;
import com.idp.repository.InvestmentDecisionEditRepository;
import com.idp.repository.InvestmentDecisionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing investment decisions (buy/sell transaction journal).
 * Handles creation, editing, closing, and querying of decision records.
 */
@Service
public class InvestmentDecisionService {

    private final InvestmentDecisionRepository decisionRepository;
    private final InvestmentDecisionEditRepository editRepository;
    private final InvestmentDecisionAlertRepository alertRepository;

    public InvestmentDecisionService(InvestmentDecisionRepository decisionRepository,
                                     InvestmentDecisionEditRepository editRepository,
                                     InvestmentDecisionAlertRepository alertRepository) {
        this.decisionRepository = decisionRepository;
        this.editRepository = editRepository;
        this.alertRepository = alertRepository;
    }

    /**
     * Create a manual decision from Investment section.
     * User specifies the price (from costBasis / shares).
     * Can be added at any time (not restricted to market hours).
     */
    @Transactional
    public InvestmentDecision createManualDecision(String userId, String symbol, DecisionType action,
                                                   BigDecimal quantity, BigDecimal price, LocalDate transactionDate) {
        // Check for duplicate
        Integer quantityInt = quantity.intValue();
        Optional<InvestmentDecision> existing = decisionRepository
            .findByUserIdAndSymbolAndTransactionDateAndActionAndQuantityAndPrice(
                userId, symbol, transactionDate, action, quantityInt, price);
        if (existing.isPresent()) {
            return existing.get();
        }

        InvestmentDecision decision = new InvestmentDecision();
        decision.setUserId(userId);
        decision.setSymbol(symbol);
        decision.setAction(action);
        decision.setQuantity(quantityInt);
        decision.setPrice(price);
        decision.setTransactionDate(transactionDate);
        decision.setSource(DecisionSource.MANUAL);

        // Auto-generate title
        String actionStr = action == DecisionType.BUY ? "Buy" : "Sell";
        String quantityStr = quantity.stripTrailingZeros().toPlainString();
        decision.setTitle(String.format("%s %s shares of %s at $%.2f", actionStr, quantityStr, symbol, price));

        decision.setStatus(DecisionStatus.ACTIVE);

        return decisionRepository.save(decision);
    }

    /**
     * Create an automatic decision from Strategy section.
     * System uses latest market price (user cannot override).
     * Strategy executor calls this when a trade is executed.
     */
    @Transactional
    public InvestmentDecision createAutoDecision(String userId, String symbol, DecisionType action,
                                                 BigDecimal quantity, BigDecimal latestPrice, LocalDate transactionDate) {
        // Check for duplicate (same symbol, action, quantity, price, date)
        Integer quantityInt = quantity.intValue();
        Optional<InvestmentDecision> existing = decisionRepository
            .findByUserIdAndSymbolAndTransactionDateAndActionAndQuantityAndPrice(
                userId, symbol, transactionDate, action, quantityInt, latestPrice);
        if (existing.isPresent()) {
            return existing.get();
        }

        InvestmentDecision decision = new InvestmentDecision();
        decision.setUserId(userId);
        decision.setSymbol(symbol);
        decision.setAction(action);
        decision.setQuantity(quantityInt);
        decision.setPrice(latestPrice); // Latest market price, not user-overridable
        decision.setTransactionDate(transactionDate);
        decision.setSource(DecisionSource.AUTO);

        // Auto-generate title with market price
        String actionStr = action == DecisionType.BUY ? "Buy" : "Sell";
        String quantityStr = quantity.stripTrailingZeros().toPlainString();
        decision.setTitle(String.format("%s %s shares of %s at $%.2f", actionStr, quantityStr, symbol, latestPrice));

        decision.setStatus(DecisionStatus.ACTIVE);

        return decisionRepository.save(decision);
    }

    /**
     * Create a decision from a buy/sell transaction (legacy method for backward compatibility).
     * Defaults to MANUAL source.
     */
    @Transactional
    public InvestmentDecision createDecision(String userId, String symbol, DecisionType action,
                                            BigDecimal quantity, BigDecimal price, LocalDate transactionDate) {
        return createManualDecision(userId, symbol, action, quantity, price, transactionDate);
    }

    /**
     * Log decision details (thesis, evidence, risks, comments).
     * Only editable on open decisions.
     */
    @Transactional
    public InvestmentDecision logDecisionDetails(Long decisionId, String thesis, String evidence,
                                                String risks, String comments) {
        InvestmentDecision decision = getDecision(decisionId);

        // Track edits
        if (thesis != null && !thesis.equals(decision.getThesis())) {
            editRepository.save(new InvestmentDecisionEdit(decision, "thesis", decision.getThesis(), thesis));
            decision.setThesis(thesis);
        }
        if (evidence != null && !evidence.equals(decision.getEvidence())) {
            editRepository.save(new InvestmentDecisionEdit(decision, "evidence", decision.getEvidence(), evidence));
            decision.setEvidence(evidence);
        }
        if (risks != null && !risks.equals(decision.getRisks())) {
            editRepository.save(new InvestmentDecisionEdit(decision, "risks", decision.getRisks(), risks));
            decision.setRisks(risks);
        }
        if (comments != null && !comments.equals(decision.getComments())) {
            editRepository.save(new InvestmentDecisionEdit(decision, "comments", decision.getComments(), comments));
            decision.setComments(comments);
        }

        return decisionRepository.save(decision);
    }

    /**
     * Edit an open decision (thesis, evidence, risks, comments only).
     * Title/action are immutable.
     */
    @Transactional
    public InvestmentDecision editDecision(Long decisionId, String thesis, String evidence,
                                          String risks, String comments) {
        InvestmentDecision decision = getDecision(decisionId);

        if (decision.isClosed()) {
            throw new IllegalStateException("Cannot edit closed decision");
        }

        return logDecisionDetails(decisionId, thesis, evidence, risks, comments);
    }

    /**
     * Add exit criteria as an alert condition.
     */
    @Transactional
    public InvestmentDecisionAlert addExitCriteria(Long decisionId, AlertConditionType conditionType,
                                                   BigDecimal conditionValue, String description) {
        InvestmentDecision decision = getDecision(decisionId);

        if (decision.isClosed()) {
            throw new IllegalStateException("Cannot add alerts to closed decision");
        }

        InvestmentDecisionAlert alert = new InvestmentDecisionAlert(decision, conditionType, conditionValue, description);
        return alertRepository.save(alert);
    }

    /**
     * Remove an exit criteria (alert).
     */
    @Transactional
    public void removeExitCriteria(Long alertId) {
        InvestmentDecisionAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (alert.getDecision().isClosed()) {
            throw new IllegalStateException("Cannot remove alerts from closed decision");
        }

        alertRepository.delete(alert);
    }

    /**
     * Close a decision when exit alert triggers.
     * Marks as CLOSED and prevents further edits.
     */
    @Transactional
    public InvestmentDecision closeDecision(Long decisionId, BigDecimal exitPrice, BigDecimal exitPnl, String closeReason) {
        InvestmentDecision decision = getDecision(decisionId);

        decision.setStatus(DecisionStatus.CLOSED);
        decision.setExitPrice(exitPrice);
        decision.setExitPnl(exitPnl);
        decision.setCloseReason(closeReason);
        decision.setClosedAt(Instant.now());

        // Mark all alerts as closed
        List<InvestmentDecisionAlert> alerts = alertRepository.findByDecisionId(decisionId);
        for (InvestmentDecisionAlert alert : alerts) {
            alert.setStatus(AlertStatus.CLOSED);
            alertRepository.save(alert);
        }

        return decisionRepository.save(decision);
    }

    /**
     * Get a specific decision by ID.
     */
    public InvestmentDecision getDecision(Long decisionId) {
        return decisionRepository.findById(decisionId)
            .orElseThrow(() -> new RuntimeException("Decision not found"));
    }

    /**
     * Get decision journal for a user (paginated, ordered by date desc).
     */
    public Page<InvestmentDecision> getDecisionJournal(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return decisionRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable);
    }

    /**
     * Get decisions for a specific symbol.
     */
    public List<InvestmentDecision> getSymbolDecisions(String userId, String symbol) {
        return decisionRepository.findByUserIdAndSymbolOrderByTransactionDateDesc(userId, symbol);
    }

    /**
     * Get decisions within a date range.
     */
    public List<InvestmentDecision> getDecisionsByDateRange(String userId, LocalDate fromDate, LocalDate toDate) {
        return decisionRepository.findDecisionsByDateRange(userId, fromDate, toDate);
    }

    /**
     * Get open decisions for a user.
     */
    public List<InvestmentDecision> getOpenDecisions(String userId) {
        return decisionRepository.findOpenDecisions(userId);
    }

    /**
     * Get all decisions for a user.
     */
    public List<InvestmentDecision> getAllDecisions(String userId) {
        return decisionRepository.findByUserIdOrderByTransactionDateDesc(userId);
    }

    /**
     * Get edit history for a decision.
     */
    public List<InvestmentDecisionEdit> getEditHistory(Long decisionId) {
        return editRepository.findByDecisionIdOrderByEditedAtDesc(decisionId);
    }

    /**
     * Get alerts for a decision.
     */
    public List<InvestmentDecisionAlert> getAlerts(Long decisionId) {
        return alertRepository.findByDecisionId(decisionId);
    }

    /**
     * Get pending alerts for a user (for alert triggering/checking).
     */
    public List<InvestmentDecisionAlert> getPendingAlerts(String userId) {
        return alertRepository.findByDecisionUserIdAndStatus(userId, AlertStatus.PENDING);
    }

    /**
     * Trigger an alert when condition is met.
     */
    @Transactional
    public InvestmentDecisionAlert triggerAlert(Long alertId, BigDecimal currentPrice) {
        InvestmentDecisionAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found"));

        alert.setStatus(AlertStatus.TRIGGERED);
        alert.setTriggeredPrice(currentPrice);
        alert.setTriggeredAt(Instant.now());

        return alertRepository.save(alert);
    }

    /**
     * Count pending alerts for a user (for UI indicator).
     */
    public long getPendingAlertCount(String userId) {
        return alertRepository.countByDecisionUserIdAndStatus(userId, AlertStatus.PENDING);
    }
}
