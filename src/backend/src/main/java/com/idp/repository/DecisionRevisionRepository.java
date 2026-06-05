package com.idp.repository;

import com.idp.model.DecisionRevision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DecisionRevisionRepository extends JpaRepository<DecisionRevision, Long> {
    List<DecisionRevision> findByDecisionRecordIdOrderByRevisionNumber(Long decisionRecordId);

    long countByDecisionRecordId(Long decisionRecordId);
}
