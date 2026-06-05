package com.idp.service;

import com.idp.dto.BehaviorInsightResponse;
import com.idp.dto.BehaviorScorecardResponse;
import com.idp.dto.DqsComponentResponse;
import com.idp.dto.DqsComponentsResponse;
import com.idp.dto.DqsDriverResponse;
import com.idp.dto.DqsResponse;
import com.idp.model.DecisionRecord;
import com.idp.model.DecisionStatus;
import com.idp.model.DecisionReview;
import com.idp.model.ReviewStatus;
import com.idp.repository.DecisionRecordRepository;
import com.idp.repository.DecisionReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsService {
    private static final double RESEARCH_WEIGHT = 0.25;
    private static final double DISCIPLINE_WEIGHT = 0.25;
    private static final double RISK_WEIGHT = 0.20;
    private static final double CONSISTENCY_WEIGHT = 0.15;
    private static final double OUTCOME_WEIGHT = 0.15;

    private final DecisionRecordRepository decisionRecordRepository;
    private final DecisionReviewRepository decisionReviewRepository;

    public AnalyticsService(
        DecisionRecordRepository decisionRecordRepository,
        DecisionReviewRepository decisionReviewRepository
    ) {
        this.decisionRecordRepository = decisionRecordRepository;
        this.decisionReviewRepository = decisionReviewRepository;
    }

    @Transactional(readOnly = true)
    public DqsResponse dqs(String ownerId) {
        List<DecisionRecord> decisions = decisionRecordRepository.findOwned(ownerId, null, null);
        List<DecisionReview> reviews = decisionReviewRepository.findOwned(ownerId, null, null);

        int research = researchQuality(decisions);
        int discipline = decisionDiscipline(reviews);
        int risk = riskManagement(decisions);
        int consistency = strategyConsistency(decisions);
        int outcome = outcomeQuality(reviews);
        int score = weightedScore(research, discipline, risk, consistency, outcome);

        return new DqsResponse(
            score,
            0,
            new DqsComponentsResponse(
                new DqsComponentResponse(RESEARCH_WEIGHT, research),
                new DqsComponentResponse(DISCIPLINE_WEIGHT, discipline),
                new DqsComponentResponse(RISK_WEIGHT, risk),
                new DqsComponentResponse(CONSISTENCY_WEIGHT, consistency),
                new DqsComponentResponse(OUTCOME_WEIGHT, outcome)
            ),
            dqsDrivers(decisions, reviews, research, discipline, risk)
        );
    }

    @Transactional(readOnly = true)
    public BehaviorScorecardResponse behavior(String ownerId) {
        List<DecisionRecord> decisions = decisionRecordRepository.findOwned(ownerId, null, null);
        List<DecisionReview> reviews = decisionReviewRepository.findOwned(ownerId, null, null);

        int research = researchQuality(decisions);
        int risk = riskManagement(decisions);
        int fomo = fomoScore(decisions);
        int lossAversion = lossAversionScore(reviews);
        int behavior = average(List.of(fomo, lossAversion, research, risk));

        return new BehaviorScorecardResponse(
            behavior,
            fomo,
            lossAversion,
            research,
            risk,
            behaviorInsights(decisions, reviews, fomo, lossAversion, research, risk)
        );
    }

    private int researchQuality(List<DecisionRecord> decisions) {
        if (decisions.isEmpty()) {
            return 50;
        }
        double averageEvidence = decisions.stream()
            .mapToInt(decision -> Math.min(decision.getEvidence().size(), 3))
            .average()
            .orElse(0);
        return percent(averageEvidence / 3.0);
    }

    private int decisionDiscipline(List<DecisionReview> reviews) {
        if (reviews.isEmpty()) {
            return 50;
        }
        long completed = reviews.stream().filter(review -> review.getStatus() == ReviewStatus.COMPLETED).count();
        return percent((double) completed / reviews.size());
    }

    private int riskManagement(List<DecisionRecord> decisions) {
        if (decisions.isEmpty()) {
            return 50;
        }
        double averageCoverage = decisions.stream()
            .mapToDouble(decision -> (Math.min(decision.getRiskFactors().size(), 2) + Math.min(decision.getExitCriteria().size(), 2)) / 4.0)
            .average()
            .orElse(0);
        return percent(averageCoverage);
    }

    private int strategyConsistency(List<DecisionRecord> decisions) {
        if (decisions.isEmpty()) {
            return 50;
        }
        long processDecisions = decisions.stream()
            .filter(decision -> decision.getStatus() == DecisionStatus.ACTIVE || decision.getStatus() == DecisionStatus.CLOSED)
            .count();
        return percent((double) processDecisions / decisions.size());
    }

    private int outcomeQuality(List<DecisionReview> reviews) {
        List<DecisionReview> completed = reviews.stream()
            .filter(review -> review.getStatus() == ReviewStatus.COMPLETED)
            .toList();
        if (completed.isEmpty()) {
            return 50;
        }
        double averageRating = completed.stream()
            .mapToDouble(review -> (review.getThesisAccuracy() + review.getRiskAssessmentAccuracy()) / 2.0)
            .average()
            .orElse(5);
        return percent(averageRating / 10.0);
    }

    private int fomoScore(List<DecisionRecord> decisions) {
        if (decisions.isEmpty()) {
            return 50;
        }
        long lowDisciplineActive = decisions.stream()
            .filter(decision -> decision.getStatus() == DecisionStatus.ACTIVE)
            .filter(decision -> decision.getConfidence() <= 5 || decision.getEvidence().size() < 2)
            .count();
        return percent(1.0 - ((double) lowDisciplineActive / decisions.size()));
    }

    private int lossAversionScore(List<DecisionReview> reviews) {
        List<DecisionReview> completed = reviews.stream()
            .filter(review -> review.getStatus() == ReviewStatus.COMPLETED)
            .toList();
        if (completed.isEmpty()) {
            return 50;
        }
        long completeLearning = completed.stream()
            .filter(review -> review.getOutcomeSummary() != null && !review.getOutcomeSummary().isBlank())
            .filter(review -> !review.getLessonsLearned().isEmpty())
            .count();
        return percent((double) completeLearning / completed.size());
    }

    private List<DqsDriverResponse> dqsDrivers(
        List<DecisionRecord> decisions,
        List<DecisionReview> reviews,
        int research,
        int discipline,
        int risk
    ) {
        List<DqsDriverResponse> drivers = new ArrayList<>();
        if (decisions.isEmpty()) {
            drivers.add(new DqsDriverResponse("Start by recording structured decisions with evidence, risks, and exit criteria.", 0, List.of()));
            return drivers;
        }
        if (research >= 70) {
            drivers.add(new DqsDriverResponse("Evidence coverage is supporting your research process.", 8, decisionIds(decisions)));
        } else {
            drivers.add(new DqsDriverResponse("Add more evidence to strengthen decision documentation.", -8, sparseEvidenceDecisionIds(decisions)));
        }
        if (risk >= 70) {
            drivers.add(new DqsDriverResponse("Risk factors and exit criteria are consistently documented.", 7, decisionIds(decisions)));
        } else {
            drivers.add(new DqsDriverResponse("Document risks and exit criteria before reviews are due.", -7, decisionIds(decisions)));
        }
        if (!reviews.isEmpty()) {
            drivers.add(new DqsDriverResponse("Review completion is contributing to decision discipline.", discipline >= 50 ? 6 : -6, reviewDecisionIds(reviews)));
        }
        return drivers;
    }

    private List<BehaviorInsightResponse> behaviorInsights(
        List<DecisionRecord> decisions,
        List<DecisionReview> reviews,
        int fomo,
        int lossAversion,
        int research,
        int risk
    ) {
        List<BehaviorInsightResponse> insights = new ArrayList<>();
        if (decisions.isEmpty()) {
            insights.add(new BehaviorInsightResponse("neutral", "No decision pattern yet", "Record a few decisions to make behavioral coaching more useful.", List.of()));
            return insights;
        }
        insights.add(new BehaviorInsightResponse(
            research >= 70 ? "positive" : "warning",
            research >= 70 ? "Research discipline is visible" : "Research notes need more support",
            research >= 70 ? "Recent decisions include enough evidence for later review." : "Add evidence before moving decisions into active status.",
            decisionIds(decisions)
        ));
        insights.add(new BehaviorInsightResponse(
            risk >= 70 ? "positive" : "warning",
            risk >= 70 ? "Risk discipline is visible" : "Risk discipline can improve",
            risk >= 70 ? "Risk factors and exit criteria are showing up consistently." : "Write down risk factors and exit criteria so reviews can compare expectations with results.",
            decisionIds(decisions)
        ));
        insights.add(new BehaviorInsightResponse(
            fomo >= 70 ? "positive" : "warning",
            fomo >= 70 ? "Active decisions look deliberate" : "Some active decisions may need more evidence",
            fomo >= 70 ? "Active decisions are generally supported by confidence and evidence." : "Review active decisions with low confidence or sparse evidence as process signals.",
            decisionIds(decisions)
        ));
        if (!reviews.isEmpty()) {
            insights.add(new BehaviorInsightResponse(
                lossAversion >= 70 ? "positive" : "neutral",
                "Review lessons are being captured",
                lossAversion >= 70 ? "Completed reviews include outcome notes and lessons learned." : "Use completed reviews to write down lessons while the context is fresh.",
                reviewDecisionIds(reviews)
            ));
        }
        return insights;
    }

    private int weightedScore(int research, int discipline, int risk, int consistency, int outcome) {
        return (int) Math.round(
            research * RESEARCH_WEIGHT
                + discipline * DISCIPLINE_WEIGHT
                + risk * RISK_WEIGHT
                + consistency * CONSISTENCY_WEIGHT
                + outcome * OUTCOME_WEIGHT
        );
    }

    private int percent(double value) {
        return (int) Math.round(Math.max(0, Math.min(1, value)) * 100);
    }

    private int average(List<Integer> values) {
        return (int) Math.round(values.stream().mapToInt(Integer::intValue).average().orElse(50));
    }

    private List<Long> decisionIds(List<DecisionRecord> decisions) {
        return decisions.stream().map(DecisionRecord::getId).toList();
    }

    private List<Long> sparseEvidenceDecisionIds(List<DecisionRecord> decisions) {
        return decisions.stream()
            .filter(decision -> decision.getEvidence().size() < 2)
            .map(DecisionRecord::getId)
            .toList();
    }

    private List<Long> reviewDecisionIds(List<DecisionReview> reviews) {
        return reviews.stream().map(review -> review.getDecisionRecord().getId()).distinct().toList();
    }
}
