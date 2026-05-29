package com.Ishwarjit.Wolf_OVRN_backend.controller;

import com.Ishwarjit.Wolf_OVRN_backend.dto.ApiResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.CreateDropEventRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.DropEventResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.DropEventSummaryResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.UpdateDropEventRequest;
import com.Ishwarjit.Wolf_OVRN_backend.service.DropEventService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drops")
public class DropEventController {

    private final DropEventService dropEventService;

    public DropEventController(DropEventService dropEventService) {
        this.dropEventService = dropEventService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<DropEventSummaryResponse>>> list(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(page, 0), Math.max(limit, 1), org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.ok(dropEventService.list(pageable)));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<DropEventSummaryResponse>>> getUpcomingDrops() {
        return ResponseEntity.ok(ApiResponse.ok(dropEventService.getUpcomingDrops()));
    }

    @GetMapping("/previous")
    public ResponseEntity<ApiResponse<List<DropEventSummaryResponse>>> getPreviousDrops() {
        return ResponseEntity.ok(ApiResponse.ok(dropEventService.getPreviousDrops()));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<DropEventResponse>> getBySlug(@PathVariable String slug) {
        try {
            UUID id = UUID.fromString(slug);
            return ResponseEntity.ok(ApiResponse.ok(dropEventService.getById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.ok(dropEventService.getBySlug(slug)));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DropEventResponse>> create(@Valid @RequestBody CreateDropEventRequest request) {
        DropEventResponse created = dropEventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<DropEventResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDropEventRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(dropEventService.update(id, request), "Updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        dropEventService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted successfully"));
    }
}
