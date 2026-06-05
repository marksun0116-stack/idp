package com.idp.service;

import com.idp.dto.CreateDecisionRequest;
import com.idp.dto.UpdateDecisionRequest;
import com.idp.exception.DecisionNotFoundException;
import com.idp.exception.DecisionStateConflictException;
import com.idp.model.DecisionRecord;
import com.idp.model.DecisionRevision;
import com.idp.model.DecisionStatus;
import com.idp.repository.DecisionRecordRepository;
import com.idp.repository.DecisionRevisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

@Service
public class DecisionRecordService {
    private final DecisionRecordRepository decisionRecordRepository;
    private final DecisionRevisionRepository decisionRevisionRepository;
    private final DecisionReviewService decisionReviewService;

    public DecisionRecordService(
        DecisionRecordRepository decisionRecordRepository,
        DecisionRevisionRepository decisionRevisionRepository,
        DecisionReviewService decisionReviewService
    ) {
        this.decisionRecordRepository = decisionRecordRepository;
        this.decisionRevisionRepository = decisionRevisionRepository;
        this.decisionReviewService = decisionReviewService;
    }

    @Transactional
    public DecisionRecord create(String ownerId, CreateDecisionRequest request) {
        DecisionRecord record = new DecisionRecord();
        record.setOwnerId(ownerId);
        record.setTicker(request.ticker().trim().toUpperCase(Locale.US));
        record.setDecisionType(request.decisionType());
        record.setTitle(request.title().trim());
        record.setThesis(request.thesis().trim());
        record.setEvidence(new ArrayList<>(request.evidence()));
        record.setRiskFactors(new ArrayList<>(request.riskFactors()));
        record.setConfidence(request.confidence());
        record.setTimeHorizon(request.timeHorizon().trim());
        record.setExitCriteria(new ArrayList<>(request.exitCriteria()));
        record.setVisibility(request.visibility());

        DecisionRecord saved = decisionRecordRepository.save(record);
        decisionRevisionRepository.save(firstRevision(saved));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<DecisionRecord> list(String ownerId, DecisionStatus status, String ticker) {
        String normalizedTicker = ticker == null || ticker.isBlank() ? null : ticker.trim().toUpperCase(Locale.US);
        return decisionRecordRepository.findOwned(ownerId, status, normalizedTicker);
    }

    @Transactional(readOnly = true)
    public DecisionRecord get(String ownerId, Long id) {
        return findOwned(ownerId, id);
    }

    @Transactional
    public UpdateResult update(String ownerId, Long id, UpdateDecisionRequest request) {
        DecisionRecord record = findOwned(ownerId, id);
        if (record.getStatus() == DecisionStatus.CLOSED || record.getStatus() == DecisionStatus.ARCHIVED) {
            throw new DecisionStateConflictException("Closed or archived decisions cannot be updated");
        }

        record.setTitle(request.title().trim());
        record.setThesis(request.thesis().trim());
        record.setEvidence(new ArrayList<>(request.evidence()));
        record.setRiskFactors(new ArrayList<>(request.riskFactors()));
        record.setConfidence(request.confidence());
        record.setTimeHorizon(request.timeHorizon().trim());
        record.setExitCriteria(new ArrayList<>(request.exitCriteria()));

        DecisionRecord saved = decisionRecordRepository.save(record);
        DecisionRevision revision = revision(saved, nextRevisionNumber(saved.getId()));
        decisionRevisionRepository.save(revision);
        return new UpdateResult(saved, revision.getRevisionNumber());
    }

    @Transactional
    public DecisionRecord transition(String ownerId, Long id, DecisionStatus requestedStatus) {
        DecisionRecord record = findOwned(ownerId, id);
        if (!canTransition(record.getStatus(), requestedStatus)) {
            throw new DecisionStateConflictException("Invalid lifecycle transition");
        }
        boolean firstActivation = requestedStatus == DecisionStatus.ACTIVE && record.getActivatedAt() == null;
        if (firstActivation) {
            record.setActivatedAt(java.time.Instant.now());
        }
        record.setStatus(requestedStatus);
        DecisionRecord saved = decisionRecordRepository.save(record);
        if (firstActivation) {
            decisionReviewService.scheduleForActiveDecision(saved);
        }
        return saved;
    }

    private DecisionRecord findOwned(String ownerId, Long id) {
        return decisionRecordRepository.findByIdAndOwnerId(id, ownerId)
            .orElseThrow(DecisionNotFoundException::new);
    }

    private int nextRevisionNumber(Long decisionRecordId) {
        return Math.toIntExact(decisionRevisionRepository.countByDecisionRecordId(decisionRecordId) + 1);
    }

    private boolean canTransition(DecisionStatus current, DecisionStatus requested) {
        if (current == requested) {
            return true;
        }
        return switch (current) {
            case DRAFT -> requested == DecisionStatus.ACTIVE || requested == DecisionStatus.ARCHIVED;
            case ACTIVE -> requested == DecisionStatus.CLOSED || requested == DecisionStatus.ARCHIVED;
            case CLOSED -> requested == DecisionStatus.ARCHIVED;
            case ARCHIVED -> false;
        };
    }

    private DecisionRevision firstRevision(DecisionRecord record) {
        return revision(record, 1);
    }

    private DecisionRevision revision(DecisionRecord record, int revisionNumber) {
        DecisionRevision revision = new DecisionRevision();
        revision.setDecisionRecord(record);
        revision.setRevisionNumber(revisionNumber);
        revision.setTitle(record.getTitle());
        revision.setThesis(record.getThesis());
        revision.setEvidence(new ArrayList<>(record.getEvidence()));
        revision.setRiskFactors(new ArrayList<>(record.getRiskFactors()));
        revision.setConfidence(record.getConfidence());
        revision.setTimeHorizon(record.getTimeHorizon());
        revision.setExitCriteria(new ArrayList<>(record.getExitCriteria()));
        return revision;
    }

    public record UpdateResult(DecisionRecord decisionRecord, int revisionNumber) {
    }
}
