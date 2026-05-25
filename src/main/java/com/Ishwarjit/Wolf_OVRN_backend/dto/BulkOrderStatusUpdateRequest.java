package com.Ishwarjit.Wolf_OVRN_backend.dto;

import com.Ishwarjit.Wolf_OVRN_backend.entity.OrderStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BulkOrderStatusUpdateRequest {
    
    @NotEmpty(message = "Order IDs list cannot be empty")
    private List<UUID> orderIds;

    @NotNull(message = "New status is required")
    private OrderStatus newStatus;
}
