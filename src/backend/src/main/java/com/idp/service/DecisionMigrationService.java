package com.idp.service;

import com.idp.model.Decision;
import com.idp.model.DecisionCategory;
import com.idp.model.DecisionStatus;
import com.idp.model.DecisionType;
import com.idp.model.DecisionSource;
import com.idp.model.InvestmentDecision;
import com.idp.model.DecisionRecord;
import com.idp.repository.DecisionRepository;
import com.idp.repository.InvestmentDecisionRepository;
import com.idp.repository.DecisionRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Migrates data from old InvestmentDecision and DecisionRecord tables
 * to the new unified Decision table.
 */
@Service
public class DecisionMigrationService {

    private static final Logger logger = LoggerFactory.getLogger(DecisionMigrationService.class);

    private final DecisionRepository decisionRepository;
    private final InvestmentDecisionRepository investmentDecisionRepository;
    private final DecisionRecordRepository decisionRecordRepository;

    public DecisionMigrationService(
            DecisionRepository decisionRepository,
            InvestmentDecisionRepository investmentDecisionRepository,
            DecisionRecordRepository decisionRecordRepository) {
        this.decisionRepository = decisionRepository;
        this.investmentDecisionRepository = investmentDecisionRepository;
        this.decisionRecordRepository = decisionRecordRepository;
    }

    /**
     * Migrate all investment decisions to the new unified table
     */
    @Transactional
    public void migrateInvestmentDecisions() {
        try {
            List<InvestmentDecision> investmentDecisions = investmentDecisionRepository.findAll();
            logger.info("Migrating {} investment decisions...", investmentDecisions.size());

            for (InvestmentDecision old : investmentDecisions) {
                Decision newDecision = new Decision();
                newDecision.setUserId(old.getUserId());
                newDecision.setDecisionCategory(DecisionCategory.INVESTMENT);
                newDecision.setSymbol(old.getSymbol());
                newDecision.setDecisionType(old.getAction());
                newDecision.setAction(old.getAction());
                newDecision.setTitle(old.getTitle());
                newDecision.setQuantity(old.getQuantity());
                newDecision.setPrice(old.getPrice());
                newDecision.setTransactionDate(old.getTransactionDate());
                newDecision.setThesis(old.getThesis());
                newDecision.setEvidence(old.getEvidence());
                newDecision.setRisks(old.getRisks());
                newDecision.setComments(old.getComments());
                newDecision.setStatus(old.getStatus());
                newDecision.setSource(old.getSource());
                newDecision.setExitPrice(old.getExitPrice());
                newDecision.setExitPnl(old.getExitPnl());
                newDecision.setCloseReason(old.getCloseReason());

                decisionRepository.save(newDecision);
            }

            logger.info("Successfully migrated {} investment decisions", investmentDecisions.size());
        } catch (Exception e) {
            logger.error("Error migrating investment decisions", e);
        }
    }

    /**
     * Migrate all strategy decisions to the new unified table
     */
    @Transactional
    public void migrateStrategyDecisions() {
        try {
            List<DecisionRecord> strategyDecisions = decisionRecordRepository.findAll();
            logger.info("Migrating {} strategy decisions...", strategyDecisions.size());

            for (DecisionRecord old : strategyDecisions) {
                Decision newDecision = new Decision();
                newDecision.setUserId(old.getOwnerId());
                newDecision.setDecisionCategory(DecisionCategory.STRATEGY);
                newDecision.setSymbol(old.getTicker());
                newDecision.setDecisionType(old.getDecisionType());
                newDecision.setTitle(old.getTitle());
                newDecision.setThesis(old.getThesis());
                newDecision.setEvidence(String.join("; ", old.getEvidence()));
                newDecision.setRisks(String.join("; ", old.getRiskFactors()));
                newDecision.setStatus(old.getStatus());
                newDecision.setConfidence(old.getConfidence());
                newDecision.setTimeHorizon(old.getTimeHorizon());
                newDecision.setExitCriteria(String.join("; ", old.getExitCriteria()));

                decisionRepository.save(newDecision);
            }

            logger.info("Successfully migrated {} strategy decisions", strategyDecisions.size());
        } catch (Exception e) {
            logger.error("Error migrating strategy decisions", e);
        }
    }

    /**
     * Run complete migration
     */
    @Transactional
    public void runMigration() {
        logger.info("Starting decision migration...");
        migrateInvestmentDecisions();
        migrateStrategyDecisions();
        logger.info("Decision migration completed");
    }
}
