package com.Ishwarjit.Wolf_OVRN_backend.dto;

import com.Ishwarjit.Wolf_OVRN_backend.entity.SizeChart;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SizeChartResponse(
        UUID id,
        String name,
        String imageName,
        String slug,
        String url,
        String altText,
        OffsetDateTime createdAt) {

    public static SizeChartResponse from(SizeChart sc) {
        return new SizeChartResponse(
                sc.getId(),
                sc.getName(),
                sc.getImageName(),
                sc.getSlug(),
                sc.getUrl(),
                sc.getAltText(),
                sc.getCreatedAt());
    }
}
