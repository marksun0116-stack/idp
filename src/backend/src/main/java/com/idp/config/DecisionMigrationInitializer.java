package com.idp.config;

import com.idp.model.Decision;
import com.idp.model.DecisionCategory;
import com.idp.model.InvestmentDecision;
import com.idp.model.DecisionRecord;
import com.idp.repository.DecisionRepository;
import com.idp.repository.InvestmentDecisionRepository;
import com.idp.repository.DecisionRecordRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Initializes the unified Decision table by migrating data from old tables on startup.
 */
@Component
public class DecisionMigrationInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DecisionMigrationInitializer.class);

    private final DecisionRepository decisionRepository;
    private final InvestmentDecisionRepository investmentDecisionRepository;
    private final DecisionRecordRepository decisionRecordRepository;

    public DecisionMigrationInitializer(
            DecisionRepository decisionRepository,
            InvestmentDecisionRepository investmentDecisionRepository,
            DecisionRecordRepository decisionRecordRepository) {
        this.decisionRepository = decisionRepository;
        this.investmentDecisionRepository = investmentDecisionRepository;
        this.decisionRecordRepository = decisionRecordRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Only run migration if new table is empty
        if (decisionRepository.count() == 0) {
            logger.info("Decision table is empty, running migration from legacy tables...");
            migrateData();
        }
    }

    @Transactional
    private void migrateData() {
        try {
            // Migrate investment decisions
            List<InvestmentDecision> investmentDecisions = investmentDecisionRepository.findAll();
            logger.info("Migrating {} investment decisions", investmentDecisions.size());

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

            // Migrate strategy decisions
            List<DecisionRecord> strategyDecisions = decisionRecordRepository.findAll();
            logger.info("Migrating {} strategy decisions", strategyDecisions.size());

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

            long totalMigrated = decisionRepository.count();
            logger.info("Migration complete! Total decisions in unified table: {}", totalMigrated);

        } catch (Exception e) {
            logger.error("Error during decision migration", e);
        }
    }
}
