package com.Ishwarjit.Wolf_OVRN_backend.service;

import com.Ishwarjit.Wolf_OVRN_backend.dto.CreateOrderRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.OrderItemRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.OrderResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.UpdateOrderRequest;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Order;
import com.Ishwarjit.Wolf_OVRN_backend.entity.OrderItem;
import com.Ishwarjit.Wolf_OVRN_backend.entity.OrderStatus;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Product;
import com.Ishwarjit.Wolf_OVRN_backend.entity.User;
import com.Ishwarjit.Wolf_OVRN_backend.exception.ResourceNotFoundException;
import com.Ishwarjit.Wolf_OVRN_backend.repository.OrderRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.ProductRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String userId) {
        UUID uuid = UUID.fromString(userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(uuid).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String userId) {
        UUID uuid = UUID.fromString(userId);
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(normalizeAddress(request.getShippingAddress()));
        order.setBillingAddress(normalizeAddress(request.getBillingAddress()));
        order.setNotes(request.getNotes());

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + itemRequest.getProductId()));

            BigDecimal unitPrice = product.getSellingPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setSubtotal(subtotal);

            order.getItems().add(item);
            total = total.add(subtotal);
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved);
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID id, UpdateOrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }
        if (request.getTrackingNumber() != null) {
            order.setTrackingNumber(request.getTrackingNumber());
        }
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> normalizeAddress(Object address) {
        if (address == null) {
            return null;
        }
        if (address instanceof Map) {
            return (Map<String, Object>) address;
        }
        if (address instanceof String str) {
            return Map.of("fullAddress", str);
        }
        return Map.of("fullAddress", address.toString());
    }
}
