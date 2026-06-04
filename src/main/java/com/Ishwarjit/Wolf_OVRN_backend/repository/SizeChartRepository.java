package com.Ishwarjit.Wolf_OVRN_backend.repository;

import com.Ishwarjit.Wolf_OVRN_backend.entity.SizeChart;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SizeChartRepository extends JpaRepository<SizeChart, UUID> {

    List<SizeChart> findAllByOrderByCreatedAtDesc();

    Optional<SizeChart> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
