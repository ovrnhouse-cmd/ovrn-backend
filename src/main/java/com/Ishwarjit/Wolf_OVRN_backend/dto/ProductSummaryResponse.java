package com.Ishwarjit.Wolf_OVRN_backend.dto;

import com.Ishwarjit.Wolf_OVRN_backend.entity.Product;
import com.Ishwarjit.Wolf_OVRN_backend.entity.ProductImage;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductSummaryResponse(
        UUID id,
        String name,
        String slug,
        BigDecimal sellingPrice,
        BigDecimal markedPrice,
        boolean inStock,
        boolean isActive,
        String primaryImageUrl) {

    public static ProductSummaryResponse from(Product product, String primaryImageUrl) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getSellingPrice(),
                product.getMarkedPrice(),
                Boolean.TRUE.equals(product.getInStock()),
                Boolean.TRUE.equals(product.getIsActive()),
                primaryImageUrl);
    }

    public static ProductSummaryResponse from(Product product, List<ProductImage> images) {
        String url = images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(ProductImage::getUrl)
                .findFirst()
                .orElseGet(() -> images.isEmpty() ? null : images.get(0).getUrl());
        return from(product, url);
    }
}
