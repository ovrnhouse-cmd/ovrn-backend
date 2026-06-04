package com.Ishwarjit.Wolf_OVRN_backend.controller;

import com.Ishwarjit.Wolf_OVRN_backend.dto.ApiResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.SizeChartRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.SizeChartResponse;
import com.Ishwarjit.Wolf_OVRN_backend.service.SizeChartService;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Global size chart management.
 *
 * Public  : GET /api/size-charts          — list all (for admin product-form dropdown)
 *           GET /api/size-charts/{id}     — single chart
 *
 * Admin   : POST   /api/size-charts       — upload (multipart: file + name)
 *           PATCH  /api/size-charts/{id}  — update name / altText
 *           DELETE /api/size-charts/{id}  — remove
 */
@RestController
@RequestMapping("/api/size-charts")
public class SizeChartController {

    private final SizeChartService sizeChartService;

    public SizeChartController(SizeChartService sizeChartService) {
        this.sizeChartService = sizeChartService;
    }

    /** [PUBLIC] List all global size charts — used by the admin product-form dropdown. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SizeChartResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(sizeChartService.findAll()));
    }

    /** [PUBLIC] Get a single size chart by UUID. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SizeChartResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(sizeChartService.findById(id)));
    }

    /**
     * [ADMIN] Upload a new global size chart.
     *
     * multipart/form-data fields:
     *   file  — image binary
     *   name  — display name shown in dropdowns (e.g. "Heavy Tee Size Guide")
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<SizeChartResponse>> create(
            @RequestPart("file") MultipartFile file,
            @RequestParam("name") String name) throws IOException {
        SizeChartResponse response = sizeChartService.create(name, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    /**
     * [ADMIN] Update a size chart's name and/or altText.
     * The image itself cannot be changed — delete and re-upload instead.
     */
    @PatchMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<ApiResponse<SizeChartResponse>> update(
            @PathVariable UUID id,
            @RequestBody SizeChartRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(sizeChartService.update(id, request), "Updated successfully"));
    }

    /** [ADMIN] Delete a global size chart. Existing product links will be set to NULL. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sizeChartService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted successfully"));
    }
}
