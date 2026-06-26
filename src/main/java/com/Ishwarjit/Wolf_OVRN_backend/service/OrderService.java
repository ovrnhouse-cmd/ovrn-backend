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
    private final StoreStatusService storeStatusService;
    private final DiscountService discountService;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            StoreStatusService storeStatusService,
            DiscountService discountService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.storeStatusService = storeStatusService;
        this.discountService = discountService;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByStatus(status, pageable).map(OrderResponse::from);
        }
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

        order.setNotes(request.getNotes());

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + itemRequest.getProductId()));

            BigDecimal unitPrice = product.getSellingPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            String resolvedSize = null;
            if (itemRequest.getSizeId() != null) {
                resolvedSize = product.getSizes().stream()
                        .filter(s -> s.getId().equals(itemRequest.getSizeId()))
                        .findFirst()
                        .map(com.Ishwarjit.Wolf_OVRN_backend.entity.Size::getSizeName)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid size for product"));
            }

            String resolvedColor = null;
            if (itemRequest.getColorId() != null) {
                resolvedColor = product.getColors().stream()
                        .filter(c -> c.getId().equals(itemRequest.getColorId()))
                        .findFirst()
                        .map(com.Ishwarjit.Wolf_OVRN_backend.entity.Color::getColorName)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid color for product"));
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setSubtotal(subtotal);
            item.setSize(resolvedSize);
            item.setColor(resolvedColor);

            order.getItems().add(item);
            total = total.add(subtotal);
        }

        com.Ishwarjit.Wolf_OVRN_backend.entity.StoreStatus storeStatus = storeStatusService.getOrCreateStatus();
        BigDecimal shippingFee = BigDecimal.ZERO;
        if (total.compareTo(storeStatus.getFreeShippingThreshold()) < 0) {
            shippingFee = storeStatus.getStandardShippingFee();
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getDiscountCode() != null && !request.getDiscountCode().isBlank()) {
            com.Ishwarjit.Wolf_OVRN_backend.dto.DiscountValidationResponse validation = discountService.validateDiscount(request.getDiscountCode(), user.getId(), total);
            if (!validation.isValid()) {
                throw new IllegalArgumentException(validation.getMessage());
            }
            discountAmount = validation.getDiscountAmount();
            order.setDiscountCode(request.getDiscountCode().toUpperCase());
            order.setDiscountAmount(discountAmount);
            total = validation.getNewTotal();
        }

        order.setShippingFee(shippingFee);
        order.setTotalAmount(total.add(shippingFee));

        Order saved = orderRepository.save(order);
        
        if (order.getDiscountCode() != null) {
            discountService.recordDiscountUsage(order.getDiscountCode(), user, saved);
        }
        
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

    @Transactional
    public int bulkUpdateOrderStatus(com.Ishwarjit.Wolf_OVRN_backend.dto.BulkOrderStatusUpdateRequest request) {
        if (request.getOrderIds() == null || request.getOrderIds().isEmpty()) {
            throw new IllegalArgumentException("No orders specified for bulk update");
        }
        return orderRepository.bulkUpdateStatus(request.getOrderIds(), request.getNewStatus());
    }

    @Transactional(readOnly = true)
    public List<com.Ishwarjit.Wolf_OVRN_backend.dto.ProductFulfillmentDto> getFulfillmentSummary(
            List<OrderStatus> statuses, List<UUID> productIds) {
        
        if (statuses == null || statuses.isEmpty()) {
            throw new IllegalArgumentException("At least one order status must be provided");
        }

        boolean filterByProducts = (productIds != null && !productIds.isEmpty());
        // To avoid JPA "empty list" errors in IN clause, pass a dummy list if empty
        List<UUID> safeProductIds = filterByProducts ? productIds : List.of(UUID.randomUUID());

        return orderRepository.getFulfillmentAggregation(statuses, safeProductIds, filterByProducts);
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
