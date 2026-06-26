package com.Ishwarjit.Wolf_OVRN_backend.repository;

import com.Ishwarjit.Wolf_OVRN_backend.entity.Discount;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, UUID> {
    Optional<Discount> findByCodeIgnoreCase(String code);

    @Query("SELECT d FROM Discount d WHERE d.isActive = true AND (d.expiresAt IS NULL OR d.expiresAt > :now)")
    List<Discount> findActiveDiscounts(@Param("now") OffsetDateTime now);
}
