package com.Ishwarjit.Wolf_OVRN_backend.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RazorpayOrderResponse {

    private final String razorpayOrderId;
    private final Long amount;
    private final String currency;
    private final UUID orderId;
}
