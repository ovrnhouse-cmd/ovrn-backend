package com.Ishwarjit.Wolf_OVRN_backend.repository;

import com.Ishwarjit.Wolf_OVRN_backend.entity.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByUserId(UUID userId, Pageable pageable);

    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT o FROM Order o WHERE o.createdAt >= :startDate")
    List<Order> findOrdersSince(@org.springframework.data.repository.query.Param("startDate") java.time.OffsetDateTime startDate);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status != :status")
    java.math.BigDecimal sumTotalRevenueExcludingStatus(@org.springframework.data.repository.query.Param("status") com.Ishwarjit.Wolf_OVRN_backend.entity.OrderStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT new com.Ishwarjit.Wolf_OVRN_backend.dto.OrderStatusDistributionDto(CAST(o.status AS string), COUNT(o)) FROM Order o GROUP BY o.status")
    List<com.Ishwarjit.Wolf_OVRN_backend.dto.OrderStatusDistributionDto> countOrdersByStatus();

    org.springframework.data.domain.Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
