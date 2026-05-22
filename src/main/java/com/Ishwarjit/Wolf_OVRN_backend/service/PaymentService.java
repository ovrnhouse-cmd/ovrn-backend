package com.Ishwarjit.Wolf_OVRN_backend.service;

import com.Ishwarjit.Wolf_OVRN_backend.dto.PaymentVerifyRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.PaymentVerifyResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.RazorpayOrderResponse;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Order;
import com.Ishwarjit.Wolf_OVRN_backend.entity.OrderStatus;
import com.Ishwarjit.Wolf_OVRN_backend.entity.PaymentTransaction;
import com.Ishwarjit.Wolf_OVRN_backend.exception.BadRequestException;
import com.Ishwarjit.Wolf_OVRN_backend.exception.ResourceNotFoundException;
import com.Ishwarjit.Wolf_OVRN_backend.exception.UnauthorizedException;
import com.Ishwarjit.Wolf_OVRN_backend.repository.OrderRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.PaymentTransactionRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RazorpayClient razorpayClient;
    private final String keySecret;

    public PaymentService(
            OrderRepository orderRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            RazorpayClient razorpayClient,
            @Value("${app.razorpay.key-secret}") String keySecret) {
        this.orderRepository = orderRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.razorpayClient = razorpayClient;
        this.keySecret = keySecret;
    }

    @Transactional
    public RazorpayOrderResponse createRazorpayOrder(UUID orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getUser() == null || !order.getUser().getId().toString().equals(userId)) {
            throw new UnauthorizedException("You can only pay for your own orders");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in PENDING state");
        }

        long amountInPaise = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        com.razorpay.Order razorpayOrder;
        try {
            JSONObject options = new JSONObject();
            options.put("amount", amountInPaise);
            options.put("currency", "INR");
            options.put("receipt", orderId.toString());
            razorpayOrder = razorpayClient.orders.create(options);
        } catch (RazorpayException ex) {
            throw new BadRequestException("Failed to create Razorpay order: " + ex.getMessage());
        }

        String razorpayOrderId = razorpayOrder.get("id");
        
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrder(order);
        transaction.setRazorpayOrderId(razorpayOrderId);
        transaction.setAmount(order.getTotalAmount());
        transaction.setStatus("CREATED");
        paymentTransactionRepository.save(transaction);

        return new RazorpayOrderResponse(razorpayOrderId, amountInPaise, "INR", orderId);
    }

    @Transactional
    public PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request, String userId) {
        PaymentTransaction transaction = paymentTransactionRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment transaction not found for razorpay_order_id: " + request.getRazorpayOrderId()));

        Order order = transaction.getOrder();
        if (order.getUser() == null || !order.getUser().getId().toString().equals(userId)) {
            throw new AccessDeniedException("You can only verify payments on your own orders");
        }

        boolean valid;
        try {
            JSONObject attributes = new JSONObject()
                    .put("razorpay_order_id", request.getRazorpayOrderId())
                    .put("razorpay_payment_id", request.getRazorpayPaymentId())
                    .put("razorpay_signature", request.getRazorpaySignature());
            valid = Utils.verifyPaymentSignature(attributes, keySecret);
        } catch (RazorpayException ex) {
            valid = false;
        }

        if (valid) {
            transaction.setStatus("SUCCESS");
            transaction.setRazorpayPaymentId(request.getRazorpayPaymentId());
            paymentTransactionRepository.save(transaction);

            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            return new PaymentVerifyResponse(true, order.getId(), "Payment verified successfully");
        }

        transaction.setStatus("FAILED");
        paymentTransactionRepository.save(transaction);

        return new PaymentVerifyResponse(false, order.getId(), "Payment verification failed");
    }
}
