package com.Ishwarjit.Wolf_OVRN_backend.controller;

import com.Ishwarjit.Wolf_OVRN_backend.dto.ApiResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.OrderTrendDto;
import com.Ishwarjit.Wolf_OVRN_backend.dto.ProductSummaryResponse;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Product;
import com.Ishwarjit.Wolf_OVRN_backend.repository.ProductRepository;
import com.Ishwarjit.Wolf_OVRN_backend.service.AnalyticsService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Ishwarjit.Wolf_OVRN_backend.dto.DashboardSummaryDto;
import com.Ishwarjit.Wolf_OVRN_backend.dto.OrderStatusDistributionDto;
import com.Ishwarjit.Wolf_OVRN_backend.dto.OrderResponse;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalyticsController {

    private final ProductRepository productRepository;
    private final AnalyticsService analyticsService;

    @GetMapping("/products/trending")
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> getTrendingProducts(
            @RequestParam(defaultValue = "views") String by,
            @RequestParam(defaultValue = "10") int limit) {

        List<Product> products;
        if ("sales".equalsIgnoreCase(by)) {
            products = productRepository.findTrendingProductsBySales(PageRequest.of(0, limit));
        } else {
            products = productRepository.findTrendingProductsByViews(PageRequest.of(0, limit));
        }

        List<ProductSummaryResponse> responses = products.stream()
                .map(product -> ProductSummaryResponse.from(product, product.getImages()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(responses, "Trending products retrieved successfully"));
    }

    @GetMapping("/admin/analytics/orders")
    public ResponseEntity<ApiResponse<List<OrderTrendDto>>> getOrdersTrend(
            @RequestParam(defaultValue = "weekly") String range) {
        List<OrderTrendDto> trends = analyticsService.getOrderTrends(range);
        return ResponseEntity.ok(ApiResponse.ok(trends, "Order trends retrieved successfully"));
    }

    @GetMapping("/admin/analytics/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getDashboardSummary() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getDashboardSummary(), "Dashboard summary retrieved successfully"));
    }

    @GetMapping("/admin/analytics/orders/status-distribution")
    public ResponseEntity<ApiResponse<List<OrderStatusDistributionDto>>> getOrderStatusDistribution() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getOrderStatusDistribution(), "Order status distribution retrieved successfully"));
    }

    @GetMapping("/admin/analytics/orders/recent")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getRecentOrders() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getRecentOrders(), "Recent orders retrieved successfully"));
    }

    @GetMapping("/admin/analytics/products/out-of-stock")
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> getOutofStockProducts() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getOutofStockProducts(), "Out of stock products retrieved successfully"));
    }
}
