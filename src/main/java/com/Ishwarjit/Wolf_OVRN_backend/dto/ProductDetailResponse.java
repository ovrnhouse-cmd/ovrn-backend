package com.Ishwarjit.Wolf_OVRN_backend.dto;

import com.Ishwarjit.Wolf_OVRN_backend.entity.Product;
import com.Ishwarjit.Wolf_OVRN_backend.entity.ProductImage;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductDetailResponse(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal sellingPrice,
        BigDecimal markedPrice,
        boolean inStock,
        boolean isActive,
        boolean isPremium,
        CategoryResponse category,
        List<ProductImageResponse> images) {

    public static ProductDetailResponse from(Product product, List<ProductImage> images) {
        CategoryResponse category = product.getCategory() != null
                ? CategoryResponse.from(product.getCategory())
                : null;
        List<ProductImageResponse> imageDtos = images.stream()
                .map(ProductImageResponse::from)
                .toList();
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getSellingPrice(),
                product.getMarkedPrice(),
                Boolean.TRUE.equals(product.getInStock()),
                Boolean.TRUE.equals(product.getIsActive()),
                Boolean.TRUE.equals(product.getIsPremium()),
                category,
                imageDtos);
    }
}
