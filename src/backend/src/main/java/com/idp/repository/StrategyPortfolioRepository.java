package com.idp.repository;

import com.idp.model.StrategyPortfolio;
import com.idp.model.StrategyVisibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StrategyPortfolioRepository extends JpaRepository<StrategyPortfolio, Long> {
    List<StrategyPortfolio> findByOwnerIdOrderByCreatedAtDesc(String ownerId);

    List<StrategyPortfolio> findByOwnerIdAndVisibilityOrderByCreatedAtDesc(String ownerId, StrategyVisibility visibility);

    Optional<StrategyPortfolio> findByIdAndOwnerId(Long id, String ownerId);

    Optional<StrategyPortfolio> findByIdAndVisibility(Long id, StrategyVisibility visibility);
}
