package com.idp.repository;

import com.idp.model.InvestmentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvestmentAccountRepository extends JpaRepository<InvestmentAccount, Long> {
    List<InvestmentAccount> findByOwnerIdOrderByCreatedAtDesc(String ownerId);

    Optional<InvestmentAccount> findByIdAndOwnerId(Long id, String ownerId);

    boolean existsByOwnerIdAndNameIgnoreCase(String ownerId, String name);
}
