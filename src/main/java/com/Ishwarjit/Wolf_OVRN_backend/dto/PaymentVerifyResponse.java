package com.Ishwarjit.Wolf_OVRN_backend.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentVerifyResponse {

    private final boolean success;
    private final UUID orderId;
    private final String message;
}
