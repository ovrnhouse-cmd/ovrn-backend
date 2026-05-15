package com.Ishwarjit.Wolf_OVRN_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String slug;

    private String description;

    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal basePrice;

    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal compareAtPrice;

    @Min(0)
    private Integer stockQuantity;

    private Boolean isActive;

    private UUID categoryId;
}
