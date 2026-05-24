package com.Ishwarjit.Wolf_OVRN_backend.service;

import com.Ishwarjit.Wolf_OVRN_backend.dto.CreateProductRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.ImageUploadResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.ProductDetailResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.ProductImageRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.ProductSummaryResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.UpdateProductRequest;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Category;
import java.util.Locale;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Product;
import com.Ishwarjit.Wolf_OVRN_backend.entity.ProductImage;
import com.Ishwarjit.Wolf_OVRN_backend.exception.ResourceNotFoundException;
import com.Ishwarjit.Wolf_OVRN_backend.repository.CategoryRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.ProductImageRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.ProductRepository;
import com.Ishwarjit.Wolf_OVRN_backend.util.SlugUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final CloudinaryService cloudinaryService;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductImageRepository productImageRepository,
            CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> list(String search, String categorySlug, Boolean isPremium, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Pageable pageable) {
        Specification<Product> spec = buildSpecification(search, categorySlug, isPremium, minPrice, maxPrice);
        return productRepository.findAll(spec, pageable).map(product -> {
            List<ProductImage> images = productImageRepository
                    .findByProductIdOrderByDisplayOrderAsc(product.getId());
            return ProductSummaryResponse.from(product, images);
        });
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + slug));
        List<ProductImage> images = productImageRepository
                .findByProductIdOrderByDisplayOrderAsc(product.getId());
        return ProductDetailResponse.from(product, images);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        List<ProductImage> images = productImageRepository
                .findByProductIdOrderByDisplayOrderAsc(id);
        return ProductDetailResponse.from(product, images);
    }

    @Transactional
    public ProductDetailResponse create(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());

        // Generate slug from name
        String slug = SlugUtils.generate(request.getName());
        product.setSlug(slug);

        product.setDescription(request.getDescription());
        product.setSellingPrice(request.getSellingPrice());
        product.setMarkedPrice(request.getMarkedPrice());
        product.setInStock(request.getInStock());
        product.setIsActive(true);
        product.setIsPremium(Boolean.TRUE.equals(request.getIsPremium()));

        if (request.getSizes() != null) {
            product.setAvailableSizes(request.getSizes());
        }

        if (request.getSellingPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Selling price cannot be less than 0.");
        }
        if (request.getMarkedPrice() != null) {
            if (request.getMarkedPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Marked price cannot be less than 0.");
            }
            if (request.getMarkedPrice().compareTo(request.getSellingPrice()) <= 0) {
                throw new IllegalArgumentException("Marked price must be strictly greater than selling price.");
            }
        }

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            if (categories.size() != request.getCategoryIds().size()) {
                throw new ResourceNotFoundException("One or more categories not found");
            }
            product.setCategories(categories);
        }

        Product saved = productRepository.save(product);

        // Save images if provided
        List<ProductImage> savedImages = new ArrayList<>();
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (ProductImageRequest imgReq : request.getImages()) {
                ProductImage image = new ProductImage();
                image.setProduct(saved);
                image.setUrl(imgReq.getUrl());
                image.setAltText(imgReq.getAltText() != null ? imgReq.getAltText() : saved.getName());
                image.setIsPrimary(Boolean.TRUE.equals(imgReq.getIsPrimary()));
                image.setDisplayOrder(imgReq.getDisplayOrder() != null ? imgReq.getDisplayOrder() : 0);
                savedImages.add(productImageRepository.save(image));
            }
        }

        return ProductDetailResponse.from(saved, savedImages);
    }

    @Transactional
    public ProductDetailResponse update(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getSlug() != null) {
            product.setSlug(SlugUtils.generate(request.getSlug()));
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getSellingPrice() != null) {
            product.setSellingPrice(request.getSellingPrice());
        }
        if (request.getMarkedPrice() != null) {
            product.setMarkedPrice(request.getMarkedPrice());
        }
        if (request.getInStock() != null) {
            product.setInStock(request.getInStock());
        }
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }
        if (request.getIsPremium() != null) {
            product.setIsPremium(request.getIsPremium());
        }
        if (request.getSizes() != null) {
            product.setAvailableSizes(request.getSizes());
        }

        java.math.BigDecimal currentSellingPrice = request.getSellingPrice() != null ? request.getSellingPrice() : product.getSellingPrice();
        java.math.BigDecimal currentMarkedPrice = request.getMarkedPrice() != null ? request.getMarkedPrice() : product.getMarkedPrice();

        if (currentSellingPrice != null && currentSellingPrice.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Selling price cannot be less than 0.");
        }
        if (currentMarkedPrice != null) {
            if (currentMarkedPrice.compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Marked price cannot be less than 0.");
            }
            if (currentSellingPrice != null && currentMarkedPrice.compareTo(currentSellingPrice) <= 0) {
                throw new IllegalArgumentException("Marked price must be strictly greater than selling price.");
            }
        }
        if (request.getCategoryIds() != null) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            if (categories.size() != request.getCategoryIds().size()) {
                throw new ResourceNotFoundException("One or more categories not found");
            }
            product.setCategories(categories);
        }

        Product saved = productRepository.save(product);

        if (request.getImages() != null) {
            productImageRepository.deleteByProductId(saved.getId());
            for (ProductImageRequest imgReq : request.getImages()) {
                ProductImage image = new ProductImage();
                image.setProduct(saved);
                image.setUrl(imgReq.getUrl());
                image.setAltText(imgReq.getAltText() != null ? imgReq.getAltText() : saved.getName());
                image.setIsPrimary(Boolean.TRUE.equals(imgReq.getIsPrimary()));
                image.setDisplayOrder(imgReq.getDisplayOrder() != null ? imgReq.getDisplayOrder() : 0);
                productImageRepository.save(image);
            }
        }

        List<ProductImage> images = productImageRepository
                .findByProductIdOrderByDisplayOrderAsc(saved.getId());
        return ProductDetailResponse.from(saved, images);
    }

    @Transactional
    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public ImageUploadResponse addImage(UUID productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        String secureUrl = cloudinaryService.upload(file);
        long existingCount = productImageRepository.countByProductId(productId);

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl(secureUrl);
        image.setAltText(product.getName());
        image.setIsPrimary(existingCount == 0);
        image.setDisplayOrder((int) existingCount);

        ProductImage saved = productImageRepository.save(image);
        return ImageUploadResponse.from(saved);
    }

    private Specification<Product> buildSpecification(String search, String categorySlug, Boolean isPremium, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String[] terms = search.toLowerCase().trim().split("\\s+");
                Predicate[] termPredicates = new Predicate[terms.length];
                for (int i = 0; i < terms.length; i++) {
                    Predicate nameMatch = cb.like(cb.lower(root.get("name")), "%" + terms[i] + "%");
                    Predicate descMatch = cb.like(cb.lower(root.get("description")), "%" + terms[i] + "%");
                    termPredicates[i] = cb.or(nameMatch, descMatch);
                }
                predicates.add(cb.or(termPredicates));
            }
            if (categorySlug != null && !categorySlug.isBlank()) {
                Join<Product, Category> categoryJoin = root.join("categories");
                predicates.add(cb.equal(categoryJoin.get("slug"), categorySlug));
            }
            if (isPremium != null) {
                predicates.add(cb.equal(root.get("isPremium"), isPremium));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
            }
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
