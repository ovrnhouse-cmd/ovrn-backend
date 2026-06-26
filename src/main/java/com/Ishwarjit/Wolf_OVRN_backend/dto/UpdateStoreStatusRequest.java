package com.Ishwarjit.Wolf_OVRN_backend.dto;

import lombok.Data;

@Data
public class UpdateStoreStatusRequest {
    private Boolean isTakingOrders;
    private Boolean isMaintenanceMode;
    private String statusMessage;
    private java.math.BigDecimal freeShippingThreshold;
    private java.math.BigDecimal standardShippingFee;
}
