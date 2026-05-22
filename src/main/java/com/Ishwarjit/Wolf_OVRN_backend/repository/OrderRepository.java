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

}
