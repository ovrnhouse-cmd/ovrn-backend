package com.Ishwarjit.Wolf_OVRN_backend.dto;

import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDropEventRequest {
    @Size(max = 255)
    private String name;

    private String slug;
    private String description;
    private OffsetDateTime dropDate;
    private OffsetDateTime expiresAt;
    private Boolean isActive;
    private List<UUID> productIds;
}
