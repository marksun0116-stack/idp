package com.idp.controller;

import com.idp.dto.CreateDecisionRequest;
import com.idp.dto.CreateDecisionResponse;
import com.idp.dto.DecisionDetailResponse;
import com.idp.dto.DecisionListResponse;
import com.idp.dto.DecisionSummaryResponse;
import com.idp.dto.TransitionDecisionRequest;
import com.idp.dto.TransitionDecisionResponse;
import com.idp.dto.UpdateDecisionRequest;
import com.idp.dto.UpdateDecisionResponse;
import com.idp.model.DecisionRecord;
import com.idp.model.DecisionStatus;
import com.idp.service.DecisionRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/decisions")
public class DecisionRecordController {
    private final DecisionRecordService decisionRecordService;

    public DecisionRecordController(DecisionRecordService decisionRecordService) {
        this.decisionRecordService = decisionRecordService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateDecisionResponse create(@Valid @RequestBody CreateDecisionRequest request, Authentication authentication) {
        var decision = decisionRecordService.create(authentication.getName(), request);
        return new CreateDecisionResponse(decision.getId(), decision.getStatus(), decision.getCreatedAt());
    }

    @GetMapping
    public DecisionListResponse list(
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "ticker", required = false) String ticker,
        Authentication authentication
    ) {
        DecisionStatus parsedStatus = status == null || status.isBlank() ? null : DecisionStatus.fromValue(status);
        List<DecisionSummaryResponse> decisions = decisionRecordService.list(authentication.getName(), parsedStatus, ticker)
            .stream()
            .map(this::summary)
            .toList();
        return new DecisionListResponse(decisions);
    }

    @GetMapping("/{id}")
    public DecisionDetailResponse get(@PathVariable("id") Long id, Authentication authentication) {
        return detail(decisionRecordService.get(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    public UpdateDecisionResponse update(
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdateDecisionRequest request,
        Authentication authentication
    ) {
        var result = decisionRecordService.update(authentication.getName(), id, request);
        return new UpdateDecisionResponse(
            result.decisionRecord().getId(),
            result.decisionRecord().getUpdatedAt(),
            result.revisionNumber()
        );
    }

    @PostMapping("/{id}/transition")
    public TransitionDecisionResponse transition(
        @PathVariable("id") Long id,
        @Valid @RequestBody TransitionDecisionRequest request,
        Authentication authentication
    ) {
        var decision = decisionRecordService.transition(authentication.getName(), id, request.status());
        return new TransitionDecisionResponse(decision.getId(), decision.getStatus(), decision.getUpdatedAt());
    }

    private DecisionSummaryResponse summary(DecisionRecord decision) {
        return new DecisionSummaryResponse(
            decision.getId(),
            decision.getTicker(),
            decision.getDecisionType(),
            decision.getTitle(),
            decision.getConfidence(),
            decision.getStatus(),
            decision.getCreatedAt()
        );
    }

    private DecisionDetailResponse detail(DecisionRecord decision) {
        return new DecisionDetailResponse(
            decision.getId(),
            decision.getTicker(),
            decision.getDecisionType(),
            decision.getTitle(),
            decision.getThesis(),
            decision.getEvidence(),
            decision.getRiskFactors(),
            decision.getConfidence(),
            decision.getTimeHorizon(),
            decision.getExitCriteria(),
            decision.getVisibility(),
            decision.getStatus(),
            decision.getCreatedAt(),
            decision.getUpdatedAt()
        );
    }
}
