package com.Ishwarjit.Wolf_OVRN_backend.service;

import com.Ishwarjit.Wolf_OVRN_backend.dto.OrderTrendDto;
import com.Ishwarjit.Wolf_OVRN_backend.dto.DashboardSummaryDto;
import com.Ishwarjit.Wolf_OVRN_backend.dto.OrderStatusDistributionDto;
import com.Ishwarjit.Wolf_OVRN_backend.dto.OrderResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.ProductSummaryResponse;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Order;
import com.Ishwarjit.Wolf_OVRN_backend.entity.OrderStatus;
import com.Ishwarjit.Wolf_OVRN_backend.repository.OrderRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.ProductRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.UserRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DashboardSummaryDto getDashboardSummary() {
        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.countByIsActiveTrue();
        BigDecimal totalRevenue = orderRepository.sumTotalRevenueExcludingStatus(OrderStatus.CANCELLED);
        
        return new DashboardSummaryDto(totalUsers, totalOrders, totalProducts, totalRevenue);
    }

    public List<OrderStatusDistributionDto> getOrderStatusDistribution() {
        return orderRepository.countOrdersByStatus();
    }

    public List<OrderResponse> getRecentOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10))
                .getContent()
                .stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    public List<ProductSummaryResponse> getOutofStockProducts() {
        return productRepository.findByInStockFalse()
                .stream()
                .map(product -> ProductSummaryResponse.from(product, product.getImages()))
                .collect(Collectors.toList());
    }

    public List<OrderTrendDto> getOrderTrends(String range) {
        OffsetDateTime startDate = switch (range.toLowerCase()) {
            case "monthly" -> OffsetDateTime.now().minusMonths(1);
            case "quarterly" -> OffsetDateTime.now().minusMonths(3);
            case "6m" -> OffsetDateTime.now().minusMonths(6);
            case "yearly" -> OffsetDateTime.now().minusYears(1);
            case "weekly" -> OffsetDateTime.now().minusWeeks(1);
            default -> OffsetDateTime.now().minusWeeks(1); // default to weekly
        };

        List<Order> orders = orderRepository.findOrdersSince(startDate);

        // Grouping logic based on range
        // For simplicity, if range <= monthly we group by day (yyyy-MM-dd)
        // If range > monthly we group by year-month (yyyy-MM)
        boolean groupByMonth = range.equalsIgnoreCase("quarterly") || range.equalsIgnoreCase("6m") || range.equalsIgnoreCase("yearly");
        DateTimeFormatter formatter = groupByMonth ? DateTimeFormatter.ofPattern("yyyy-MM") : DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, List<Order>> groupedOrders = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().format(formatter),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<OrderTrendDto> trends = new ArrayList<>();
        groupedOrders.forEach((date, dateOrders) -> {
            long count = dateOrders.size();
            BigDecimal totalRevenue = dateOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            trends.add(new OrderTrendDto(date, count, totalRevenue));
        });

        return trends;
    }
}
