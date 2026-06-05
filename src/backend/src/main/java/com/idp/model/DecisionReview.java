package com.idp.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "decision_reviews",
    uniqueConstraints = @UniqueConstraint(columnNames = {"decision_record_id", "review_type"})
)
public class DecisionReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "decision_record_id")
    private DecisionRecord decisionRecord;

    @Column(nullable = false)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ReviewType reviewType;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant completedAt;

    @Column(length = 8000)
    private String outcomeSummary;

    private Integer thesisAccuracy;

    private Integer riskAssessmentAccuracy;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "decision_review_lessons", joinColumns = @JoinColumn(name = "decision_review_id"))
    @Column(name = "lesson", nullable = false, length = 2000)
    private List<String> lessonsLearned = new ArrayList<>();

    @Column(length = 2000)
    private String nextAction;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public DecisionRecord getDecisionRecord() {
        return decisionRecord;
    }

    public void setDecisionRecord(DecisionRecord decisionRecord) {
        this.decisionRecord = decisionRecord;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public ReviewType getReviewType() {
        return reviewType;
    }

    public void setReviewType(ReviewType reviewType) {
        this.reviewType = reviewType;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getOutcomeSummary() {
        return outcomeSummary;
    }

    public void setOutcomeSummary(String outcomeSummary) {
        this.outcomeSummary = outcomeSummary;
    }

    public Integer getThesisAccuracy() {
        return thesisAccuracy;
    }

    public void setThesisAccuracy(Integer thesisAccuracy) {
        this.thesisAccuracy = thesisAccuracy;
    }

    public Integer getRiskAssessmentAccuracy() {
        return riskAssessmentAccuracy;
    }

    public void setRiskAssessmentAccuracy(Integer riskAssessmentAccuracy) {
        this.riskAssessmentAccuracy = riskAssessmentAccuracy;
    }

    public List<String> getLessonsLearned() {
        return lessonsLearned;
    }

    public void setLessonsLearned(List<String> lessonsLearned) {
        this.lessonsLearned = lessonsLearned;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }
}
