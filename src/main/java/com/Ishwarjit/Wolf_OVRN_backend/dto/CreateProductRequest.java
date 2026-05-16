package com.Ishwarjit.Wolf_OVRN_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    private String slug;

    private String description;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal markedPrice;

    @NotNull
    private Boolean inStock;

    private UUID categoryId;

    private List<String> images;
}
