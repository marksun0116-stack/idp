package com.idp.service;

import com.idp.dto.CompleteReviewRequest;
import com.idp.exception.ReviewNotFoundException;
import com.idp.exception.ReviewStateConflictException;
import com.idp.model.DecisionRecord;
import com.idp.model.DecisionReview;
import com.idp.model.ReviewStatus;
import com.idp.model.ReviewType;
import com.idp.repository.DecisionReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class DecisionReviewService {
    private final DecisionReviewRepository decisionReviewRepository;

    public DecisionReviewService(DecisionReviewRepository decisionReviewRepository) {
        this.decisionReviewRepository = decisionReviewRepository;
    }

    @Transactional
    public void scheduleForActiveDecision(DecisionRecord decisionRecord) {
        LocalDate activatedDate = decisionRecord.getActivatedAt().atZone(ZoneOffset.UTC).toLocalDate();
        createIfMissing(decisionRecord, ReviewType.THIRTY_DAYS, activatedDate.plusDays(30));
        createIfMissing(decisionRecord, ReviewType.NINETY_DAYS, activatedDate.plusDays(90));
        createIfMissing(decisionRecord, ReviewType.ONE_EIGHTY_DAYS, activatedDate.plusDays(180));
        createIfMissing(decisionRecord, ReviewType.ONE_YEAR, activatedDate.plusYears(1));
    }

    @Transactional(readOnly = true)
    public List<DecisionReview> list(String ownerId, ReviewStatus status, Long decisionId) {
        return decisionReviewRepository.findOwned(ownerId, status, decisionId);
    }

    @Transactional
    public DecisionReview complete(String ownerId, Long reviewId, CompleteReviewRequest request) {
        DecisionReview review = decisionReviewRepository.findByIdAndOwnerId(reviewId, ownerId)
            .orElseThrow(ReviewNotFoundException::new);
        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new ReviewStateConflictException("Review state does not allow completion");
        }

        review.setOutcomeSummary(request.outcomeSummary().trim());
        review.setThesisAccuracy(request.thesisAccuracy());
        review.setRiskAssessmentAccuracy(request.riskAssessmentAccuracy());
        review.setLessonsLearned(new ArrayList<>(request.lessonsLearned()));
        review.setNextAction(request.nextAction() == null || request.nextAction().isBlank() ? null : request.nextAction().trim());
        review.setCompletedAt(Instant.now());
        review.setStatus(ReviewStatus.COMPLETED);
        return decisionReviewRepository.save(review);
    }

    private void createIfMissing(DecisionRecord decisionRecord, ReviewType reviewType, LocalDate dueDate) {
        if (decisionReviewRepository.existsByDecisionRecordIdAndReviewType(decisionRecord.getId(), reviewType)) {
            return;
        }

        DecisionReview review = new DecisionReview();
        review.setDecisionRecord(decisionRecord);
        review.setOwnerId(decisionRecord.getOwnerId());
        review.setReviewType(reviewType);
        review.setDueDate(dueDate);
        decisionReviewRepository.save(review);
    }
}
