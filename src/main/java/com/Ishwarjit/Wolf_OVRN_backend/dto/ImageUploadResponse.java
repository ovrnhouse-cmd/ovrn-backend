package com.Ishwarjit.Wolf_OVRN_backend.dto;

import com.Ishwarjit.Wolf_OVRN_backend.entity.ProductImage;
import java.util.UUID;

public record ImageUploadResponse(
        UUID id,
        UUID productId,
        String url,
        String imageName,
        String slug,
        boolean isPrimary,
        int displayOrder) {

    public static ImageUploadResponse from(ProductImage image) {
        return new ImageUploadResponse(
                image.getId(),
                image.getProduct().getId(),
                image.getUrl(),
                image.getImageName(),
                image.getSlug(),
                Boolean.TRUE.equals(image.getIsPrimary()),
                image.getDisplayOrder() != null ? image.getDisplayOrder() : 0);
    }
}
