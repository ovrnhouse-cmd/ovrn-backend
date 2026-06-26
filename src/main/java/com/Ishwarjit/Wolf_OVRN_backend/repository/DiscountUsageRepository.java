package com.Ishwarjit.Wolf_OVRN_backend.repository;

import com.Ishwarjit.Wolf_OVRN_backend.entity.DiscountUsage;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountUsageRepository extends JpaRepository<DiscountUsage, UUID> {
    boolean existsByUserIdAndDiscountId(UUID userId, UUID discountId);
}
