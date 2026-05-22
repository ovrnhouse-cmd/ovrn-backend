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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.Ishwarjit.Wolf_OVRN_backend.dto.ProductImageRequest;
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
    private final com.Ishwarjit.Wolf_OVRN_backend.service.CloudinaryService cloudinaryService;

    public ProductController(ProductService productService, com.Ishwarjit.Wolf_OVRN_backend.service.CloudinaryService cloudinaryService) {
        this.productService = productService;
        this.cloudinaryService = cloudinaryService;
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
        try {
            UUID id = UUID.fromString(slug);
            return ResponseEntity.ok(ApiResponse.ok(productService.getById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.ok(productService.getBySlug(slug)));
        }
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createJson(@Valid @RequestBody CreateProductRequest request) {
        ProductDetailResponse created = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createMultipart(
            @RequestPart("product") @Valid CreateProductRequest request,
            @RequestPart(value = "primaryImage", required = false) MultipartFile primaryImage,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        
        List<ProductImageRequest> imageRequests = new ArrayList<>();
        
        if (primaryImage != null && !primaryImage.isEmpty()) {
            validateImage(primaryImage);
            String primaryUrl = cloudinaryService.upload(primaryImage);
            ProductImageRequest pir = new ProductImageRequest();
            pir.setUrl(primaryUrl);
            pir.setIsPrimary(true);
            pir.setDisplayOrder(0);
            pir.setAltText(request.getName());
            imageRequests.add(pir);
        }
        
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                if (file != null && !file.isEmpty()) {
                    validateImage(file);
                    String url = cloudinaryService.upload(file);
                    ProductImageRequest pir = new ProductImageRequest();
                    pir.setUrl(url);
                    pir.setIsPrimary(false);
                    pir.setDisplayOrder(i + 1);
                    pir.setAltText(request.getName());
                    imageRequests.add(pir);
                }
            }
        }
        
        if (!imageRequests.isEmpty()) {
            request.setImages(imageRequests);
        }
        
        ProductDetailResponse created = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PatchMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateJson(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productService.update(id, request), "Updated successfully"));
    }

    @PatchMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateMultipart(
            @PathVariable UUID id,
            @RequestPart("product") @Valid UpdateProductRequest request,
            @RequestPart(value = "primaryImage", required = false) MultipartFile primaryImage,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        
        List<ProductImageRequest> imageRequests = new ArrayList<>();
        
        if (primaryImage != null && !primaryImage.isEmpty()) {
            validateImage(primaryImage);
            String primaryUrl = cloudinaryService.upload(primaryImage);
            ProductImageRequest pir = new ProductImageRequest();
            pir.setUrl(primaryUrl);
            pir.setIsPrimary(true);
            pir.setDisplayOrder(0);
            pir.setAltText(request.getName() != null ? request.getName() : "Product Image");
            imageRequests.add(pir);
        }
        
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                if (file != null && !file.isEmpty()) {
                    validateImage(file);
                    String url = cloudinaryService.upload(file);
                    ProductImageRequest pir = new ProductImageRequest();
                    pir.setUrl(url);
                    pir.setIsPrimary(false);
                    pir.setDisplayOrder(i + 1);
                    pir.setAltText(request.getName() != null ? request.getName() : "Product Image");
                    imageRequests.add(pir);
                }
            }
        }
        
        List<ProductImageRequest> combined = new ArrayList<>();
        if (request.getImages() != null) {
            combined.addAll(request.getImages());
        }
        combined.addAll(imageRequests);
        request.setImages(combined);
        
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

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds the limit of 10MB: " + file.getOriginalFilename());
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed: " + file.getOriginalFilename());
        }
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
