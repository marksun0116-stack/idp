package com.idp.repository;

import com.idp.model.StrategyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StrategyTransactionRepository extends JpaRepository<StrategyTransaction, Long> {
    List<StrategyTransaction> findByStrategyIdOrderByExecutedAtAsc(Long strategyId);

    boolean existsByStrategyIdAndSymbol(Long strategyId, String symbol);
}
