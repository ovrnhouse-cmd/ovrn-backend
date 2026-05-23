package com.Ishwarjit.Wolf_OVRN_backend.dto;

import java.math.BigDecimal;

public record DashboardSummaryDto(
        long totalUsers,
        long totalOrders,
        long totalProducts,
        BigDecimal totalRevenue) {
}
