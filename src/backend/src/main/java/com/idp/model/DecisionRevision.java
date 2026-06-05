package com.idp.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "decision_revisions")
public class DecisionRevision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "decision_record_id")
    private DecisionRecord decisionRecord;

    @Column(nullable = false)
    private int revisionNumber;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 8000)
    private String thesis;

    @ElementCollection
    @CollectionTable(name = "decision_revision_evidence", joinColumns = @JoinColumn(name = "decision_revision_id"))
    @Column(name = "evidence", nullable = false, length = 2000)
    private List<String> evidence = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "decision_revision_risk_factors", joinColumns = @JoinColumn(name = "decision_revision_id"))
    @Column(name = "risk_factor", nullable = false, length = 2000)
    private List<String> riskFactors = new ArrayList<>();

    @Column(nullable = false)
    private int confidence;

    @Column(nullable = false)
    private String timeHorizon;

    @ElementCollection
    @CollectionTable(name = "decision_revision_exit_criteria", joinColumns = @JoinColumn(name = "decision_revision_id"))
    @Column(name = "exit_criterion", nullable = false, length = 2000)
    private List<String> exitCriteria = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

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

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(int revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThesis() {
        return thesis;
    }

    public void setThesis(String thesis) {
        this.thesis = thesis;
    }

    public List<String> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<String> evidence) {
        this.evidence = evidence;
    }

    public List<String> getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(List<String> riskFactors) {
        this.riskFactors = riskFactors;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    public String getTimeHorizon() {
        return timeHorizon;
    }

    public void setTimeHorizon(String timeHorizon) {
        this.timeHorizon = timeHorizon;
    }

    public List<String> getExitCriteria() {
        return exitCriteria;
    }

    public void setExitCriteria(List<String> exitCriteria) {
        this.exitCriteria = exitCriteria;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
