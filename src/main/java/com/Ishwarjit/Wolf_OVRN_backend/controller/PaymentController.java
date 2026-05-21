package com.Ishwarjit.Wolf_OVRN_backend.controller;

import com.Ishwarjit.Wolf_OVRN_backend.dto.ApiResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.PaymentVerifyRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.PaymentVerifyResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.RazorpayOrderRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.RazorpayOrderResponse;
import com.Ishwarjit.Wolf_OVRN_backend.exception.UnauthorizedException;
import com.Ishwarjit.Wolf_OVRN_backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createOrder(
            @Valid @RequestBody RazorpayOrderRequest request) {
        RazorpayOrderResponse created =
                paymentService.createRazorpayOrder(request.getOrderId(), currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentVerifyResponse>> verify(
            @Valid @RequestBody PaymentVerifyRequest request) {
        PaymentVerifyResponse result = paymentService.verifyPayment(request, currentUserId());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("No authenticated user");
        }
        return authentication.getPrincipal().toString();
    }
}
