package com.Ishwarjit.Wolf_OVRN_backend.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Request body for:
 *   POST  /api/size-charts  (name required, altText optional — file comes as multipart part)
 *   PATCH /api/size-charts/{id}  (all fields optional)
 */
@Getter
@Setter
public class SizeChartRequest {

    private String name;

    private String altText;
}
