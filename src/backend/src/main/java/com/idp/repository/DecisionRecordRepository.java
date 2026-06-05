package com.idp.repository;

import com.idp.model.DecisionRecord;
import com.idp.model.DecisionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DecisionRecordRepository extends JpaRepository<DecisionRecord, Long> {
    Optional<DecisionRecord> findByIdAndOwnerId(Long id, String ownerId);

    @Query("""
        select decision
        from DecisionRecord decision
        where decision.ownerId = :ownerId
          and (:status is null or decision.status = :status)
          and (:ticker is null or decision.ticker = :ticker)
        order by decision.createdAt desc
        """)
    List<DecisionRecord> findOwned(
        @Param("ownerId") String ownerId,
        @Param("status") DecisionStatus status,
        @Param("ticker") String ticker
    );
}
