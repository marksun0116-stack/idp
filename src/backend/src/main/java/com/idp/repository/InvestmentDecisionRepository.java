package com.idp.repository;

import com.idp.model.InvestmentDecision;
import com.idp.model.DecisionStatus;
import com.idp.model.DecisionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvestmentDecisionRepository extends JpaRepository<InvestmentDecision, Long> {

    /**
     * Get all decisions for a user, ordered by transaction date (newest first) - paginated
     */
    Page<InvestmentDecision> findByUserIdOrderByTransactionDateDesc(String userId, Pageable pageable);

    /**
     * Get all decisions for a user, ordered by transaction date (newest first)
     */
    List<InvestmentDecision> findByUserIdOrderByTransactionDateDesc(String userId);

    /**
     * Get decisions for a user and symbol
     */
    List<InvestmentDecision> findByUserIdAndSymbolOrderByTransactionDateDesc(String userId, String symbol);

    /**
     * Get decisions for a user within a date range
     */
    @Query("SELECT d FROM InvestmentDecision d WHERE d.userId = :userId " +
           "AND d.transactionDate BETWEEN :fromDate AND :toDate " +
           "ORDER BY d.transactionDate DESC")
    List<InvestmentDecision> findDecisionsByDateRange(
        @Param("userId") String userId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    /**
     * Get open decisions for a user
     */
    @Query("SELECT d FROM InvestmentDecision d WHERE d.userId = :userId " +
           "AND d.status = com.idp.model.DecisionStatus.ACTIVE " +
           "ORDER BY d.transactionDate DESC")
    List<InvestmentDecision> findOpenDecisions(@Param("userId") String userId);

    /**
     * Get decisions by symbol and action (for tracking buy/sell history)
     */
    List<InvestmentDecision> findByUserIdAndSymbolAndActionOrderByTransactionDateDesc(
        String userId, String symbol, DecisionType action
    );

    /**
     * Check if a decision exists for a transaction (prevents duplicates)
     */
    Optional<InvestmentDecision> findByUserIdAndSymbolAndTransactionDateAndActionAndQuantityAndPrice(
        String userId, String symbol, LocalDate transactionDate, DecisionType action, Integer quantity, java.math.BigDecimal price
    );
}
