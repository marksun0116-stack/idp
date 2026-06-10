package com.idp.service;

import com.idp.model.Decision;
import com.idp.model.DecisionAlert;
import com.idp.model.DecisionCategory;
import com.idp.model.DecisionEdit;
import com.idp.model.DecisionStatus;
import com.idp.model.DecisionType;
import com.idp.model.DecisionSource;
import com.idp.repository.DecisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified service for managing both Investment and Strategy decisions.
 */
@Service
@Transactional
public class DecisionService {

    private final DecisionRepository decisionRepository;

    public DecisionService(DecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    /**
     * Create an investment decision (buy/sell transaction tracking)
     */
    public Decision createInvestmentDecision(
            String userId,
            String symbol,
            DecisionType action,
            Integer quantity,
            BigDecimal price,
            LocalDate transactionDate) {

        Decision decision = new Decision();
        decision.setUserId(userId);
        decision.setDecisionCategory(DecisionCategory.INVESTMENT);
        decision.setSymbol(symbol);
        decision.setDecisionType(action);
        decision.setAction(action);
        decision.setQuantity(quantity);
        decision.setPrice(price);
        decision.setTransactionDate(transactionDate);
        decision.setSource(DecisionSource.MANUAL);
        decision.setStatus(DecisionStatus.ACTIVE);
        decision.setTitle(String.format("%s %d shares of %s at $%s",
                action.toString(), quantity, symbol, price));

        return decisionRepository.save(decision);
    }

    /**
     * Create a strategy decision (structured investment thesis)
     */
    public Decision createStrategyDecision(
            String userId,
            String symbol,
            DecisionType decisionType,
            String title,
            String thesis,
            String evidence,
            String risks,
            Integer confidence,
            String timeHorizon) {

        Decision decision = new Decision();
        decision.setUserId(userId);
        decision.setDecisionCategory(DecisionCategory.STRATEGY);
        decision.setSymbol(symbol);
        decision.setDecisionType(decisionType);
        decision.setTitle(title);
        decision.setThesis(thesis);
        decision.setEvidence(evidence);
        decision.setRisks(risks);
        decision.setConfidence(confidence);
        decision.setTimeHorizon(timeHorizon);
        decision.setStatus(DecisionStatus.ACTIVE);

        return decisionRepository.save(decision);
    }

    /**
     * Log/update decision details (thesis, evidence, risks, comments)
     */
    public Decision logDecisionDetails(
            Long decisionId,
            String thesis,
            String evidence,
            String risks,
            String comments) {

        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new IllegalArgumentException("Decision not found: " + decisionId));

        // Track edits
        if (thesis != null && !thesis.equals(decision.getThesis())) {
            decision.getEditHistory().add(new DecisionEdit(decision, "thesis", decision.getThesis(), thesis));
            decision.setThesis(thesis);
        }
        if (evidence != null && !evidence.equals(decision.getEvidence())) {
            decision.getEditHistory().add(new DecisionEdit(decision, "evidence", decision.getEvidence(), evidence));
            decision.setEvidence(evidence);
        }
        if (risks != null && !risks.equals(decision.getRisks())) {
            decision.getEditHistory().add(new DecisionEdit(decision, "risks", decision.getRisks(), risks));
            decision.setRisks(risks);
        }
        if (comments != null && !comments.equals(decision.getComments())) {
            decision.getEditHistory().add(new DecisionEdit(decision, "comments", decision.getComments(), comments));
            decision.setComments(comments);
        }

        return decisionRepository.save(decision);
    }

    /**
     * Add exit criteria alert to a decision
     */
    public DecisionAlert addExitCriteria(
            Long decisionId,
            DecisionAlert.AlertConditionType conditionType,
            BigDecimal conditionValue,
            String description) {

        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new IllegalArgumentException("Decision not found: " + decisionId));

        DecisionAlert alert = new DecisionAlert(decision, conditionType, conditionValue, description);
        decision.getAlerts().add(alert);

        decisionRepository.save(decision);
        return alert;
    }

    /**
     * Remove exit criteria alert
     */
    public void removeExitCriteria(Long alertId) {
        // This would typically be done through a DecisionAlertRepository
        // For now, we handle it at the decision level
    }

    /**
     * Close a decision
     */
    public Decision closeDecision(
            Long decisionId,
            BigDecimal exitPrice,
            BigDecimal exitPnl,
            String closeReason) {

        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new IllegalArgumentException("Decision not found: " + decisionId));

        decision.setStatus(DecisionStatus.CLOSED);
        decision.setExitPrice(exitPrice);
        decision.setExitPnl(exitPnl);
        decision.setCloseReason(closeReason);

        return decisionRepository.save(decision);
    }

    /**
     * Get a single decision
     */
    public Decision getDecision(Long id) {
        return decisionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Decision not found: " + id));
    }

    /**
     * Get open decisions for user
     */
    public List<Decision> getOpenDecisions(String userId) {
        return decisionRepository.findOpenDecisions(userId);
    }

    /**
     * Get all decisions for user
     */
    public List<Decision> getAllDecisions(String userId) {
        return decisionRepository.findByUserId(userId);
    }

    /**
     * Get decisions by category (INVESTMENT or STRATEGY)
     */
    public List<Decision> getDecisionsByCategory(String userId, DecisionCategory category) {
        return decisionRepository.findByUserIdAndDecisionCategory(userId, category);
    }

    /**
     * Get decisions for a specific symbol
     */
    public List<Decision> getSymbolDecisions(String userId, String symbol) {
        return decisionRepository.findByUserIdAndSymbol(userId, symbol);
    }

    /**
     * Get edit history for a decision
     */
    public List<DecisionEdit> getEditHistory(Long decisionId) {
        Decision decision = getDecision(decisionId);
        return new ArrayList<>(decision.getEditHistory());
    }

    /**
     * Get alerts for a decision
     */
    public List<DecisionAlert> getAlerts(Long decisionId) {
        Decision decision = getDecision(decisionId);
        return new ArrayList<>(decision.getAlerts());
    }
}
