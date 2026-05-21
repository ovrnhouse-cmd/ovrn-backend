package com.Ishwarjit.Wolf_OVRN_backend.controller;

import com.Ishwarjit.Wolf_OVRN_backend.dto.ApiResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.CartItemRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.CartResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.UpdateCartItemRequest;
import com.Ishwarjit.Wolf_OVRN_backend.exception.UnauthorizedException;
import com.Ishwarjit.Wolf_OVRN_backend.service.CartService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart(currentUserId())));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Valid @RequestBody CartItemRequest request) {
        CartResponse cart = cartService.addItem(currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(cart));
    }

    @PatchMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok(cartService.updateItem(currentUserId(), productId, request), "Updated successfully"));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@PathVariable UUID productId) {
        return ResponseEntity.ok(
                ApiResponse.ok(cartService.removeItem(currentUserId(), productId), "Removed successfully"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<CartResponse>> clearCart() {
        return ResponseEntity.ok(
                ApiResponse.ok(cartService.clearCart(currentUserId()), "Cart cleared"));
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("No authenticated user");
        }
        return authentication.getPrincipal().toString();
    }
}
