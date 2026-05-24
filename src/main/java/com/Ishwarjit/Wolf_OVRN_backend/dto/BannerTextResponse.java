package com.Ishwarjit.Wolf_OVRN_backend.dto;

import com.Ishwarjit.Wolf_OVRN_backend.entity.BannerText;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;

/**
 * Read model returned to clients.
 * The admin endpoints also include isActive/sortOrder so admins can manage ordering.
 */
@Getter
public class BannerTextResponse {

    private final UUID id;
    private final String text;
    private final boolean isHighlight;
    private final int sortOrder;
    private final boolean isActive;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public BannerTextResponse(BannerText entity) {
        this.id = entity.getId();
        this.text = entity.getText();
        this.isHighlight = entity.isHighlight();
        this.sortOrder = entity.getSortOrder();
        this.isActive = entity.isActive();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }
}
