package com.idp.repository;

import com.idp.model.InvestmentDecisionAlert;
import com.idp.model.InvestmentDecisionAlert.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestmentDecisionAlertRepository extends JpaRepository<InvestmentDecisionAlert, Long> {

    /**
     * Get all alerts for a decision
     */
    List<InvestmentDecisionAlert> findByDecisionId(Long decisionId);

    /**
     * Get pending alerts for a user (to check if they should trigger)
     */
    List<InvestmentDecisionAlert> findByDecisionUserIdAndStatus(String userId, AlertStatus status);

    /**
     * Get all alerts for a decision with a specific status
     */
    List<InvestmentDecisionAlert> findByDecisionIdAndStatus(Long decisionId, AlertStatus status);

    /**
     * Count pending alerts for a user (to show in UI)
     */
    long countByDecisionUserIdAndStatus(String userId, AlertStatus status);
}
