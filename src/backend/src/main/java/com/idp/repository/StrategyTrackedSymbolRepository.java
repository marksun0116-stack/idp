package com.idp.repository;

import com.idp.model.StrategyTrackedSymbol;
import com.idp.model.SymbolVisibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StrategyTrackedSymbolRepository extends JpaRepository<StrategyTrackedSymbol, Long> {
    boolean existsByStrategyIdAndSymbol(Long strategyId, String symbol);

    Optional<StrategyTrackedSymbol> findByStrategyIdAndSymbol(Long strategyId, String symbol);

    List<StrategyTrackedSymbol> findByStrategyIdOrderBySymbol(Long strategyId);

    List<StrategyTrackedSymbol> findByStrategyIdAndVisibilityOrderBySymbol(Long strategyId, SymbolVisibility visibility);
}
