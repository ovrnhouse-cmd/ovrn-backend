package com.Ishwarjit.Wolf_OVRN_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageRequest {

    @NotBlank
    private String url;

    private Boolean isPrimary = false;

    private Integer displayOrder = 0;

    private String altText;
}
