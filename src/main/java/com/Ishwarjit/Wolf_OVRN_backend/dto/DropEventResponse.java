package com.Ishwarjit.Wolf_OVRN_backend.dto;

import com.Ishwarjit.Wolf_OVRN_backend.entity.DropEvent;
import com.Ishwarjit.Wolf_OVRN_backend.entity.ProductImage;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public record DropEventResponse(
        UUID id,
        String name,
        String slug,
        String description,
        OffsetDateTime dropDate,
        boolean isActive,
        List<ProductSummaryResponse> products) {

    public static DropEventResponse from(DropEvent dropEvent, Map<UUID, List<ProductImage>> productImagesMap) {
        List<ProductSummaryResponse> productDtos = dropEvent.getProducts().stream()
                .map(p -> ProductSummaryResponse.from(p, productImagesMap.getOrDefault(p.getId(), List.of())))
                .collect(Collectors.toList());

        return new DropEventResponse(
                dropEvent.getId(),
                dropEvent.getName(),
                dropEvent.getSlug(),
                dropEvent.getDescription(),
                dropEvent.getDropDate(),
                Boolean.TRUE.equals(dropEvent.getIsActive()),
                productDtos);
    }

    public static DropEventResponse fromWithoutProducts(DropEvent dropEvent) {
        return new DropEventResponse(
                dropEvent.getId(),
                dropEvent.getName(),
                dropEvent.getSlug(),
                dropEvent.getDescription(),
                dropEvent.getDropDate(),
                Boolean.TRUE.equals(dropEvent.getIsActive()),
                null);
    }
}
