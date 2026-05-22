package com.Ishwarjit.Wolf_OVRN_backend.repository;

import com.Ishwarjit.Wolf_OVRN_backend.entity.PaymentTransaction;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    Optional<PaymentTransaction> findByRazorpayOrderId(String razorpayOrderId);
}
