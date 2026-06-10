package com.idp.repository;

import com.idp.model.Decision;
import com.idp.model.DecisionCategory;
import com.idp.model.DecisionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, Long> {

    // Find all decisions for a user
    List<Decision> findByUserId(String userId);

    // Find decisions by user and status
    List<Decision> findByUserIdAndStatus(String userId, DecisionStatus status);

    // Find decisions by user and category
    List<Decision> findByUserIdAndDecisionCategory(String userId, DecisionCategory category);

    // Find open/active decisions for a user
    @Query("SELECT d FROM Decision d WHERE d.userId = :userId AND d.status IN ('ACTIVE', 'DRAFT') ORDER BY d.createdAt DESC")
    List<Decision> findOpenDecisions(@Param("userId") String userId);

    // Find decisions by symbol
    List<Decision> findByUserIdAndSymbol(String userId, String symbol);

    // Find decisions by user, category, and status
    List<Decision> findByUserIdAndDecisionCategoryAndStatus(String userId, DecisionCategory category, DecisionStatus status);

    // Find a specific decision and verify ownership
    Optional<Decision> findByIdAndUserId(Long id, String userId);

    // Count open decisions for a user
    long countByUserIdAndStatus(String userId, DecisionStatus status);

    // Count decisions by category
    long countByUserIdAndDecisionCategory(String userId, DecisionCategory category);
}
