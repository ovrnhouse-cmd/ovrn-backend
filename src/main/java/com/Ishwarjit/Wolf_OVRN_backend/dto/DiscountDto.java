package com.Ishwarjit.Wolf_OVRN_backend.dto;

import com.Ishwarjit.Wolf_OVRN_backend.entity.Discount;
import com.Ishwarjit.Wolf_OVRN_backend.entity.DiscountType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DiscountDto {
    private UUID id;
    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minPurchaseAmount;
    private Integer maxUses;
    private Integer currentUses;
    private Boolean isOneTimePerUser;
    private Boolean isActive;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;

    public static DiscountDto from(Discount discount) {
        return new DiscountDto(
                discount.getId(),
                discount.getCode(),
                discount.getDescription(),
                discount.getDiscountType(),
                discount.getDiscountValue(),
                discount.getMinPurchaseAmount(),
                discount.getMaxUses(),
                discount.getCurrentUses(),
                discount.getIsOneTimePerUser(),
                discount.getIsActive(),
                discount.getExpiresAt(),
                discount.getCreatedAt()
        );
    }
}
