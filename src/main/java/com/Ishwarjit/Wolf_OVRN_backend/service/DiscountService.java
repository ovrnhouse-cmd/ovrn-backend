package com.Ishwarjit.Wolf_OVRN_backend.service;

import com.Ishwarjit.Wolf_OVRN_backend.dto.CreateDiscountRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.DiscountDto;
import com.Ishwarjit.Wolf_OVRN_backend.dto.DiscountValidationResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.UpdateDiscountRequest;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Discount;
import com.Ishwarjit.Wolf_OVRN_backend.entity.DiscountType;
import com.Ishwarjit.Wolf_OVRN_backend.entity.DiscountUsage;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Order;
import com.Ishwarjit.Wolf_OVRN_backend.entity.User;
import com.Ishwarjit.Wolf_OVRN_backend.exception.ResourceNotFoundException;
import com.Ishwarjit.Wolf_OVRN_backend.repository.DiscountRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.DiscountUsageRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final DiscountUsageRepository discountUsageRepository;

    public DiscountService(DiscountRepository discountRepository, DiscountUsageRepository discountUsageRepository) {
        this.discountRepository = discountRepository;
        this.discountUsageRepository = discountUsageRepository;
    }

    @Transactional(readOnly = true)
    public List<DiscountDto> getAllDiscounts() {
        return discountRepository.findAll().stream()
                .map(DiscountDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DiscountDto> getActiveDiscounts() {
        return discountRepository.findActiveDiscounts(OffsetDateTime.now()).stream()
                .filter(d -> d.getMaxUses() == null || d.getCurrentUses() < d.getMaxUses())
                .map(DiscountDto::from)
                .toList();
    }

    @Transactional
    public DiscountDto createDiscount(CreateDiscountRequest request) {
        Discount discount = new Discount();
        discount.setCode(request.getCode().toUpperCase());
        discount.setDescription(request.getDescription());
        discount.setDiscountType(request.getDiscountType());
        discount.setDiscountValue(request.getDiscountValue());
        discount.setMinPurchaseAmount(request.getMinPurchaseAmount());
        discount.setMaxUses(request.getMaxUses());
        discount.setIsOneTimePerUser(request.getIsOneTimePerUser() != null ? request.getIsOneTimePerUser() : false);
        discount.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        discount.setExpiresAt(request.getExpiresAt());

        return DiscountDto.from(discountRepository.save(discount));
    }

    @Transactional
    public DiscountDto updateDiscount(UUID id, UpdateDiscountRequest request) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));

        if (request.getDescription() != null) discount.setDescription(request.getDescription());
        if (request.getMaxUses() != null) discount.setMaxUses(request.getMaxUses());
        if (request.getIsOneTimePerUser() != null) discount.setIsOneTimePerUser(request.getIsOneTimePerUser());
        if (request.getIsActive() != null) discount.setIsActive(request.getIsActive());
        if (request.getExpiresAt() != null) discount.setExpiresAt(request.getExpiresAt());

        return DiscountDto.from(discountRepository.save(discount));
    }

    @Transactional
    public void deleteDiscount(UUID id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
        discountRepository.delete(discount);
    }

    @Transactional(readOnly = true)
    public DiscountValidationResponse validateDiscount(String code, UUID userId, BigDecimal cartTotal) {
        Discount discount = discountRepository.findByCodeIgnoreCase(code)
                .orElse(null);

        if (discount == null) {
            return new DiscountValidationResponse(false, "Invalid discount code", BigDecimal.ZERO, cartTotal);
        }

        if (!discount.getIsActive()) {
            return new DiscountValidationResponse(false, "Discount code is no longer active", BigDecimal.ZERO, cartTotal);
        }

        if (discount.getExpiresAt() != null && discount.getExpiresAt().isBefore(OffsetDateTime.now())) {
            return new DiscountValidationResponse(false, "Discount code has expired", BigDecimal.ZERO, cartTotal);
        }

        if (discount.getMaxUses() != null && discount.getCurrentUses() >= discount.getMaxUses()) {
            return new DiscountValidationResponse(false, "Discount code usage limit reached", BigDecimal.ZERO, cartTotal);
        }

        if (discount.getMinPurchaseAmount() != null && cartTotal.compareTo(discount.getMinPurchaseAmount()) < 0) {
            return new DiscountValidationResponse(false, "Minimum purchase amount not reached", BigDecimal.ZERO, cartTotal);
        }

        if (discount.getIsOneTimePerUser()) {
            boolean alreadyUsed = discountUsageRepository.existsByUserIdAndDiscountId(userId, discount.getId());
            if (alreadyUsed) {
                return new DiscountValidationResponse(false, "You have already used this discount code", BigDecimal.ZERO, cartTotal);
            }
        }

        BigDecimal discountAmount = calculateDiscountAmount(discount, cartTotal);
        BigDecimal newTotal = cartTotal.subtract(discountAmount);
        
        // Ensure total doesn't go below 0
        if (newTotal.compareTo(BigDecimal.ZERO) < 0) {
            discountAmount = cartTotal;
            newTotal = BigDecimal.ZERO;
        }

        return new DiscountValidationResponse(true, "Discount applied successfully", discountAmount, newTotal);
    }

    public BigDecimal calculateDiscountAmount(Discount discount, BigDecimal cartTotal) {
        if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
            BigDecimal percentage = discount.getDiscountValue().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            return cartTotal.multiply(percentage).setScale(2, RoundingMode.HALF_UP);
        } else {
            return discount.getDiscountValue();
        }
    }

    @Transactional
    public void recordDiscountUsage(String code, User user, Order order) {
        Discount discount = discountRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));

        discount.setCurrentUses(discount.getCurrentUses() + 1);
        discountRepository.save(discount);

        DiscountUsage usage = new DiscountUsage();
        usage.setUser(user);
        usage.setDiscount(discount);
        usage.setOrder(order);
        discountUsageRepository.save(usage);
    }
}
