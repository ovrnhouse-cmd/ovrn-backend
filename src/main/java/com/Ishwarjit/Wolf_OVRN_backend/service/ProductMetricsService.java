package com.Ishwarjit.Wolf_OVRN_backend.service;

import com.Ishwarjit.Wolf_OVRN_backend.entity.ProductMetricLog;
import com.Ishwarjit.Wolf_OVRN_backend.entity.ProductMetricType;
import com.Ishwarjit.Wolf_OVRN_backend.entity.ProductStat;
import com.Ishwarjit.Wolf_OVRN_backend.repository.ProductMetricLogRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.ProductStatRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductMetricsService {

    private final ProductMetricLogRepository productMetricLogRepository;
    private final ProductStatRepository productStatRepository;

    @Async
    @Transactional
    public void logProductAction(UUID productId, ProductMetricType actionType, UUID userId) {
        try {
            // 1. Log the raw event
            ProductMetricLog logEntry = new ProductMetricLog();
            logEntry.setProductId(productId);
            logEntry.setActionType(actionType);
            logEntry.setUserId(userId);
            productMetricLogRepository.save(logEntry);

            // 2. Update aggregate stats
            ProductStat stat = productStatRepository.findById(productId)
                    .orElseGet(() -> {
                        ProductStat newStat = new ProductStat();
                        newStat.setProductId(productId);
                        newStat.setTotalViews(0L);
                        newStat.setTotalSales(0L);
                        newStat.setTotalCartAdditions(0L);
                        return newStat;
                    });

            switch (actionType) {
                case VIEW -> stat.setTotalViews(stat.getTotalViews() + 1);
                case ADD_TO_CART -> stat.setTotalCartAdditions(stat.getTotalCartAdditions() + 1);
                case PURCHASE -> stat.setTotalSales(stat.getTotalSales() + 1);
            }

            stat.setUpdatedAt(OffsetDateTime.now());
            productStatRepository.save(stat);

        } catch (Exception e) {
            log.error("Failed to log product metric action: {}", actionType, e);
        }
    }
}
