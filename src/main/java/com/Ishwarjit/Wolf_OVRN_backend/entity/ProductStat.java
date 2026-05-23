package com.Ishwarjit.Wolf_OVRN_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "product_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductStat {

    @Id
    @Column(name = "product_id", updatable = false, nullable = false)
    private UUID productId;

    @Column(name = "total_views", nullable = false)
    private Long totalViews = 0L;

    @Column(name = "total_sales", nullable = false)
    private Long totalSales = 0L;

    @Column(name = "total_cart_additions", nullable = false)
    private Long totalCartAdditions = 0L;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
