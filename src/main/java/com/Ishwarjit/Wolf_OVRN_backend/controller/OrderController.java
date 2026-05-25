package com.Ishwarjit.Wolf_OVRN_backend.controller;

import com.Ishwarjit.Wolf_OVRN_backend.dto.ApiResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.CreateOrderRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.OrderResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.UpdateOrderRequest;
import com.Ishwarjit.Wolf_OVRN_backend.exception.UnauthorizedException;
import com.Ishwarjit.Wolf_OVRN_backend.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String sort) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(limit, 1), parseSort(sort));
        return ResponseEntity.ok(ApiResponse.ok(orderService.getAllOrders(pageable)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> myOrders() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getMyOrders(currentUserId())));
    }

    @GetMapping("/fulfillment")
    public ResponseEntity<ApiResponse<List<com.Ishwarjit.Wolf_OVRN_backend.dto.ProductFulfillmentDto>>> getFulfillmentSummary(
            @RequestParam(required = true) List<com.Ishwarjit.Wolf_OVRN_backend.entity.OrderStatus> statuses,
            @RequestParam(required = false) List<UUID> productIds) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getFulfillmentSummary(statuses, productIds)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.createOrder(request, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.updateOrderStatus(id, request), "Updated successfully"));
    }

    @PatchMapping("/bulk-status")
    public ResponseEntity<ApiResponse<Integer>> bulkUpdateStatus(
            @Valid @RequestBody com.Ishwarjit.Wolf_OVRN_backend.dto.BulkOrderStatusUpdateRequest request) {
        int updatedCount = orderService.bulkUpdateOrderStatus(request);
        return ResponseEntity.ok(ApiResponse.ok(updatedCount, "Bulk update successful"));
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("No authenticated user");
        }
        return authentication.getPrincipal().toString();
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        if (field.isEmpty()) {
            return Sort.unsorted();
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            String dir = parts[1].trim();
            if ("desc".equalsIgnoreCase(dir)) {
                direction = Sort.Direction.DESC;
            }
        }
        return Sort.by(direction, field);
    }
}
