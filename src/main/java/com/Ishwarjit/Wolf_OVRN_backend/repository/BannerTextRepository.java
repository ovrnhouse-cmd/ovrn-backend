package com.Ishwarjit.Wolf_OVRN_backend.repository;

import com.Ishwarjit.Wolf_OVRN_backend.entity.BannerText;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BannerTextRepository extends JpaRepository<BannerText, UUID> {

    /**
     * Returns only active banner items, sorted by sortOrder ascending.
     * Used by the public endpoint.
     */
    List<BannerText> findByIsActiveTrueOrderBySortOrderAsc();

    /**
     * Returns all banner items (active + inactive), sorted by sortOrder.
     * Used by the admin endpoint.
     */
    List<BannerText> findAllByOrderBySortOrderAsc();
}
