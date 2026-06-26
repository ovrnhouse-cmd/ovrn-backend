package com.Ishwarjit.Wolf_OVRN_backend.dto;

import com.Ishwarjit.Wolf_OVRN_backend.entity.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDiscountRequest {
    @NotBlank
    private String code;
    
    private String description;
    
    @NotNull
    private DiscountType discountType;
    
    @NotNull
    private BigDecimal discountValue;
    
    private BigDecimal minPurchaseAmount;
    
    private Integer maxUses;
    
    private Boolean isOneTimePerUser = false;
    
    private Boolean isActive = true;
    
    private OffsetDateTime expiresAt;
}
