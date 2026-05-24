package com.Ishwarjit.Wolf_OVRN_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BannerTextRequest {

    @NotBlank(message = "Text must not be blank")
    @Size(max = 500, message = "Text must not exceed 500 characters")
    private String text;

    /** When true, the frontend renders this item with the accent/highlight CSS class. */
    private boolean isHighlight = false;

    /** Display order — lower numbers appear first in the banner strip. */
    private int sortOrder = 0;

    /** When false, this item is hidden on the public banner (soft delete). */
    private boolean isActive = true;
}
