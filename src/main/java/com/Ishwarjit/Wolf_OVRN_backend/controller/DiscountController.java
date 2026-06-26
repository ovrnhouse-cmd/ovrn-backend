package com.Ishwarjit.Wolf_OVRN_backend.controller;

import com.Ishwarjit.Wolf_OVRN_backend.dto.ApiResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.CreateDiscountRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.DiscountDto;
import com.Ishwarjit.Wolf_OVRN_backend.dto.DiscountValidationResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.UpdateDiscountRequest;
import com.Ishwarjit.Wolf_OVRN_backend.service.DiscountService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/discounts")
    public ResponseEntity<ApiResponse<List<DiscountDto>>> getAllDiscounts() {
        return ResponseEntity.ok(ApiResponse.ok(discountService.getAllDiscounts(), "Discounts retrieved successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/discounts")
    public ResponseEntity<ApiResponse<DiscountDto>> createDiscount(@Valid @RequestBody CreateDiscountRequest request) {
        DiscountDto discount = discountService.createDiscount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(discount, "Discount created successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/discounts/{id}")
    public ResponseEntity<ApiResponse<DiscountDto>> updateDiscount(@PathVariable UUID id, @RequestBody UpdateDiscountRequest request) {
        DiscountDto discount = discountService.updateDiscount(id, request);
        return ResponseEntity.ok(ApiResponse.ok(discount, "Discount updated successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/discounts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDiscount(@PathVariable UUID id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Discount deleted successfully"));
    }

    @PostMapping("/discounts/validate")
    public ResponseEntity<ApiResponse<DiscountValidationResponse>> validateDiscount(
            @RequestBody Map<String, Object> payload) {
        
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "You must be logged in to validate a discount"));
        }

        String code = (String) payload.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Discount code is required"));
        }

        Object cartTotalObj = payload.get("cartTotal");
        if (cartTotalObj == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Cart total is required"));
        }
        
        BigDecimal cartTotal = new BigDecimal(cartTotalObj.toString());
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());

        DiscountValidationResponse response = discountService.validateDiscount(code, userId, cartTotal);
        if (response.isValid()) {
            return ResponseEntity.ok(ApiResponse.ok(response, response.getMessage()));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), response.getMessage()));
        }
    }

    @GetMapping("/discounts")
    public ResponseEntity<ApiResponse<List<DiscountDto>>> getActiveDiscounts() {
        return ResponseEntity.ok(ApiResponse.ok(discountService.getActiveDiscounts(), "Active discounts retrieved successfully"));
    }
}
