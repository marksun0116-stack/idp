package com.idp.repository;

import com.idp.model.InvestmentDecisionEdit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestmentDecisionEditRepository extends JpaRepository<InvestmentDecisionEdit, Long> {

    /**
     * Get edit history for a decision, ordered by edit time (newest first)
     */
    List<InvestmentDecisionEdit> findByDecisionIdOrderByEditedAtDesc(Long decisionId);

    /**
     * Get edits for a specific field
     */
    List<InvestmentDecisionEdit> findByDecisionIdAndFieldNameOrderByEditedAtDesc(Long decisionId, String fieldName);
}
