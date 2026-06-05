package com.idp.repository;

import com.idp.model.PublicProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublicProfileRepository extends JpaRepository<PublicProfile, Long> {
    Optional<PublicProfile> findByOwnerId(String ownerId);

    Optional<PublicProfile> findByHandle(String handle);

    boolean existsByHandleAndOwnerIdNot(String handle, String ownerId);
}
