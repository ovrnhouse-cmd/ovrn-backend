package com.Ishwarjit.Wolf_OVRN_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDropEventRequest {
    @NotBlank
    @Size(max = 255)
    private String name;

    private String slug;
    private String description;

    @NotNull
    private OffsetDateTime dropDate;

    @NotNull
    private OffsetDateTime expiresAt;

    private Boolean isActive = true;
    private List<UUID> productIds;
}
