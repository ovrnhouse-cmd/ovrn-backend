package com.Ishwarjit.Wolf_OVRN_backend.dto;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDiscountRequest {
    private String description;
    private Integer maxUses;
    private Boolean isOneTimePerUser;
    private Boolean isActive;
    private OffsetDateTime expiresAt;
}
