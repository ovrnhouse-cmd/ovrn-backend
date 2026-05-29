package com.Ishwarjit.Wolf_OVRN_backend.dto;

import com.Ishwarjit.Wolf_OVRN_backend.entity.DropEvent;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DropEventSummaryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        OffsetDateTime dropDate,
        boolean isActive) {

    public static DropEventSummaryResponse from(DropEvent dropEvent) {
        return new DropEventSummaryResponse(
                dropEvent.getId(),
                dropEvent.getName(),
                dropEvent.getSlug(),
                dropEvent.getDescription(),
                dropEvent.getDropDate(),
                Boolean.TRUE.equals(dropEvent.getIsActive()));
    }
}
