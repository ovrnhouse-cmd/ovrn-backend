package com.Ishwarjit.Wolf_OVRN_backend.repository;

import com.Ishwarjit.Wolf_OVRN_backend.entity.Product;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);

    @Query("SELECT p FROM Product p JOIN ProductStat ps ON p.id = ps.productId WHERE p.isActive = true ORDER BY ps.totalViews DESC")
    List<Product> findTrendingProductsByViews(Pageable pageable);

    @Query("SELECT p FROM Product p JOIN ProductStat ps ON p.id = ps.productId WHERE p.isActive = true ORDER BY ps.totalSales DESC")
    List<Product> findTrendingProductsBySales(Pageable pageable);

    long countByIsActiveTrue();

    List<Product> findByInStockFalse();
}
