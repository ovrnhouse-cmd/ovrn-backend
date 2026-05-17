package com.Ishwarjit.Wolf_OVRN_backend.controller;

import com.Ishwarjit.Wolf_OVRN_backend.dto.ApiResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.CreateProductRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.ImageUploadResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.ProductDetailResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.ProductSummaryResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.UpdateProductRequest;
import com.Ishwarjit.Wolf_OVRN_backend.service.ProductService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isPremium,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) String sort) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(limit, 1), parseSort(sort));
        return ResponseEntity.ok(ApiResponse.ok(productService.list(search, category, isPremium, minPrice, maxPrice, pageable)));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> get(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getBySlug(slug)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetailResponse>> create(@Valid @RequestBody CreateProductRequest request) {
        ProductDetailResponse created = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productService.update(id, request), "Updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted successfully"));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file) throws IOException {
        ImageUploadResponse response = productService.addImage(id, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.unsorted();
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        if (field.isEmpty()) {
            return Sort.unsorted();
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            String dir = parts[1].trim();
            if ("desc".equalsIgnoreCase(dir)) {
                direction = Sort.Direction.DESC;
            }
        }
        return Sort.by(direction, field);
    }
}
