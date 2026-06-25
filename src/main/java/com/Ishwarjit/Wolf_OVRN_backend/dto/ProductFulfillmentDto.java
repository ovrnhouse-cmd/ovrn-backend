package com.Ishwarjit.Wolf_OVRN_backend.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductFulfillmentDto {
    private UUID productId;
    private String productName;
    private String color;
    private String size;
    private String fit;
    private Long totalQuantity;
}
