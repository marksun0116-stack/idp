package com.idp.repository;

import com.idp.model.DecisionReview;
import com.idp.model.ReviewStatus;
import com.idp.model.ReviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DecisionReviewRepository extends JpaRepository<DecisionReview, Long> {
    boolean existsByDecisionRecordIdAndReviewType(Long decisionRecordId, ReviewType reviewType);

    Optional<DecisionReview> findByIdAndOwnerId(Long id, String ownerId);

    List<DecisionReview> findByDecisionRecordIdOrderByDueDate(Long decisionRecordId);

    @Query("""
        select review
        from DecisionReview review
        where review.ownerId = :ownerId
          and (:status is null or review.status = :status)
          and (:decisionId is null or review.decisionRecord.id = :decisionId)
        order by review.dueDate asc
        """)
    List<DecisionReview> findOwned(
        @Param("ownerId") String ownerId,
        @Param("status") ReviewStatus status,
        @Param("decisionId") Long decisionId
    );
}
