package com.Ishwarjit.Wolf_OVRN_backend.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DiscountValidationResponse {
    private boolean valid;
    private String message;
    private BigDecimal discountAmount;
    private BigDecimal newTotal;
}
