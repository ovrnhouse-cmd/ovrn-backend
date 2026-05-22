package com.Ishwarjit.Wolf_OVRN_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;

    @NotNull
    private Object shippingAddress;

    private Object billingAddress;

    private String notes;
}
