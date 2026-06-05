package com.idp.controller;

import com.idp.dto.CompleteReviewRequest;
import com.idp.dto.CompleteReviewResponse;
import com.idp.dto.ReviewListResponse;
import com.idp.dto.ReviewSummaryResponse;
import com.idp.model.DecisionReview;
import com.idp.model.ReviewStatus;
import com.idp.service.DecisionReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class DecisionReviewController {
    private final DecisionReviewService decisionReviewService;

    public DecisionReviewController(DecisionReviewService decisionReviewService) {
        this.decisionReviewService = decisionReviewService;
    }

    @GetMapping
    public ReviewListResponse list(
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "decisionId", required = false) Long decisionId,
        Authentication authentication
    ) {
        ReviewStatus parsedStatus = status == null || status.isBlank() ? null : ReviewStatus.fromValue(status);
        List<ReviewSummaryResponse> reviews = decisionReviewService.list(authentication.getName(), parsedStatus, decisionId)
            .stream()
            .map(this::summary)
            .toList();
        return new ReviewListResponse(reviews);
    }

    @PostMapping("/{id}/complete")
    public CompleteReviewResponse complete(
        @PathVariable("id") Long id,
        @Valid @RequestBody CompleteReviewRequest request,
        Authentication authentication
    ) {
        DecisionReview review = decisionReviewService.complete(authentication.getName(), id, request);
        return new CompleteReviewResponse(review.getId(), review.getStatus(), review.getCompletedAt());
    }

    private ReviewSummaryResponse summary(DecisionReview review) {
        return new ReviewSummaryResponse(
            review.getId(),
            review.getDecisionRecord().getId(),
            review.getReviewType(),
            review.getDueDate(),
            review.getStatus(),
            review.getCompletedAt()
        );
    }
}
