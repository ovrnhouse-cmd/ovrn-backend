package com.Ishwarjit.Wolf_OVRN_backend.dto;

import com.Ishwarjit.Wolf_OVRN_backend.entity.StoreStatus;

public record StoreStatusResponse(
        boolean isTakingOrders,
        boolean isMaintenanceMode,
        String statusMessage,
        java.math.BigDecimal freeShippingThreshold,
        java.math.BigDecimal standardShippingFee) {

    public static StoreStatusResponse from(StoreStatus entity) {
        return new StoreStatusResponse(
                entity.isTakingOrders(),
                entity.isMaintenanceMode(),
                entity.getStatusMessage(),
                entity.getFreeShippingThreshold(),
                entity.getStandardShippingFee()
        );
    }
}
