package com.Ishwarjit.Wolf_OVRN_backend.dto;

import com.Ishwarjit.Wolf_OVRN_backend.entity.Product;
import com.Ishwarjit.Wolf_OVRN_backend.entity.ProductImage;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record ProductSummaryResponse(
        UUID id,
        String name,
        String slug,
        BigDecimal sellingPrice,
        BigDecimal markedPrice,
        boolean inStock,
        boolean isActive,
        boolean isPremium,
        String primaryImageUrl,
        List<String> availableSizes,
        List<CategoryResponse> categories,
        String description) {

    public static ProductSummaryResponse from(Product product, String primaryImageUrl) {
        List<CategoryResponse> categoryResponses = product.getCategories() == null ? List.of() : 
            product.getCategories().stream().map(CategoryResponse::from).collect(Collectors.toList());

        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getSellingPrice(),
                product.getMarkedPrice(),
                Boolean.TRUE.equals(product.getInStock()),
                Boolean.TRUE.equals(product.getIsActive()),
                Boolean.TRUE.equals(product.getIsPremium()),
                primaryImageUrl,
                product.getAvailableSizes() == null ? List.of() : product.getAvailableSizes(),
                categoryResponses,
                product.getDescription());
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
