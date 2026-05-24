package com.Ishwarjit.Wolf_OVRN_backend.controller;

import com.Ishwarjit.Wolf_OVRN_backend.dto.ApiResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.BannerTextRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.BannerTextResponse;
import com.Ishwarjit.Wolf_OVRN_backend.entity.BannerText;
import com.Ishwarjit.Wolf_OVRN_backend.exception.ResourceNotFoundException;
import com.Ishwarjit.Wolf_OVRN_backend.repository.BannerTextRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Banner Text CRUD API.
 *
 * Public (no auth):
 *   GET /api/banner-texts          — returns only active items, sorted by sortOrder
 *
 * Admin only (ROLE_ADMIN):
 *   GET    /api/banner-texts/all   — returns ALL items (active + inactive)
 *   POST   /api/banner-texts       — create a new banner text item
 *   PUT    /api/banner-texts/{id}  — update an existing item
 *   DELETE /api/banner-texts/{id}  — permanently delete an item
 */
@RestController
@RequestMapping("/api/banner-texts")
public class BannerTextController {

    private final BannerTextRepository bannerTextRepository;

    public BannerTextController(BannerTextRepository bannerTextRepository) {
        this.bannerTextRepository = bannerTextRepository;
    }

    // ──────────────────────────── PUBLIC ────────────────────────────

    /**
     * Returns only active banner items ordered by sortOrder.
     * Frontend uses this to populate the scrolling banner strip.
     *
     * Response shape per item:
     *  {
     *    "id": "...",
     *    "text": "▌ DROP 002 — HOWL SEASON // LOADING",
     *    "isHighlight": false,   ← if true, render with accent CSS class
     *    "sortOrder": 0,
     *    "isActive": true,
     *    "createdAt": "...",
     *    "updatedAt": "..."
     *  }
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BannerTextResponse>>> getActiveBannerTexts() {
        List<BannerTextResponse> items = bannerTextRepository
                .findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(BannerTextResponse::new)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    // ──────────────────────────── ADMIN ─────────────────────────────

    /**
     * Returns ALL banner texts (active + inactive) for admin management.
     * Security rule in SecurityConfig permits only ROLE_ADMIN.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BannerTextResponse>>> getAllBannerTexts() {
        List<BannerTextResponse> items = bannerTextRepository
                .findAllByOrderBySortOrderAsc()
                .stream()
                .map(BannerTextResponse::new)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    /** Create a new banner text item. */
    @PostMapping
    public ResponseEntity<ApiResponse<BannerTextResponse>> createBannerText(
            @Valid @RequestBody BannerTextRequest request) {

        BannerText entity = new BannerText();
        applyRequest(entity, request);
        BannerText saved = bannerTextRepository.save(entity);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(new BannerTextResponse(saved)));
    }

    /** Replace all editable fields of an existing banner text item. */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BannerTextResponse>> updateBannerText(
            @PathVariable UUID id,
            @Valid @RequestBody BannerTextRequest request) {

        BannerText entity = bannerTextRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner text not found with id: " + id));

        applyRequest(entity, request);
        BannerText saved = bannerTextRepository.save(entity);
        return ResponseEntity.ok(ApiResponse.ok(new BannerTextResponse(saved), "Banner text updated successfully"));
    }

    /** Permanently delete a banner text item by ID. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBannerText(@PathVariable UUID id) {
        if (!bannerTextRepository.existsById(id)) {
            throw new ResourceNotFoundException("Banner text not found with id: " + id);
        }
        bannerTextRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Banner text deleted successfully"));
    }

    // ──────────────────────────── HELPERS ───────────────────────────

    private void applyRequest(BannerText entity, BannerTextRequest request) {
        entity.setText(request.getText());
        entity.setHighlight(request.isHighlight());
        entity.setSortOrder(request.getSortOrder());
        entity.setActive(request.isActive());
    }
}
