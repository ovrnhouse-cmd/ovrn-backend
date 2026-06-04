package com.Ishwarjit.Wolf_OVRN_backend.dto;

import com.Ishwarjit.Wolf_OVRN_backend.entity.ProductImage;
import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        String url,
        String imageName,
        String slug,
        String altText,
        boolean isPrimary,
        int displayOrder) {

    public static ProductImageResponse from(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getUrl(),
                image.getImageName(),
                image.getSlug(),
                image.getAltText(),
                Boolean.TRUE.equals(image.getIsPrimary()),
                image.getDisplayOrder() != null ? image.getDisplayOrder() : 0);
    }
}
