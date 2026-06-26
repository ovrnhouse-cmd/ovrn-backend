package com.Ishwarjit.Wolf_OVRN_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "store_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "is_taking_orders", nullable = false)
    private boolean isTakingOrders = true;

    @Column(name = "is_maintenance_mode", nullable = false)
    private boolean isMaintenanceMode = false;

    @Column(name = "status_message")
    private String statusMessage;

    @Column(name = "free_shipping_threshold", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal freeShippingThreshold = new java.math.BigDecimal("1599.00");

    @Column(name = "standard_shipping_fee", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal standardShippingFee = new java.math.BigDecimal("40.00");

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
