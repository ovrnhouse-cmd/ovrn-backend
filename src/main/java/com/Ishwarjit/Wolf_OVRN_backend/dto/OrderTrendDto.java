package com.Ishwarjit.Wolf_OVRN_backend.dto;

import java.math.BigDecimal;

public record OrderTrendDto(
        String date,
        long orderCount,
        BigDecimal totalRevenue) {
}
