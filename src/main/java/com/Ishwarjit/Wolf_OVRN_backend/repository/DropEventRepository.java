package com.Ishwarjit.Wolf_OVRN_backend.repository;

import com.Ishwarjit.Wolf_OVRN_backend.entity.DropEvent;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DropEventRepository extends JpaRepository<DropEvent, UUID> {
    Optional<DropEvent> findBySlug(String slug);

    Optional<DropEvent> findFirstByIsActiveTrueAndDropDateAfterOrderByDropDateAsc(OffsetDateTime now);

    @Query("SELECT d FROM DropEvent d WHERE d.isActive = true AND d.dropDate <= :now ORDER BY d.dropDate DESC")
    List<DropEvent> findPreviousDrops(OffsetDateTime now);
}
