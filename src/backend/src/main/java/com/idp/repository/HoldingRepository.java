package com.idp.repository;

import com.idp.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByAccountOwnerIdOrderBySymbol(String ownerId);

    List<Holding> findByAccountIdOrderBySymbol(Long accountId);

    Optional<Holding> findByIdAndAccountId(Long id, Long accountId);

    boolean existsByAccountIdAndSymbol(Long accountId, String symbol);
}
