package com.Ishwarjit.Wolf_OVRN_backend.service;

import com.Ishwarjit.Wolf_OVRN_backend.dto.CreateProductRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.ImageUploadResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.ProductDetailResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.ProductImageRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.ProductImageResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.ProductSummaryResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.UpdateProductRequest;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Category;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Product;
import com.Ishwarjit.Wolf_OVRN_backend.entity.ProductImage;
import com.Ishwarjit.Wolf_OVRN_backend.entity.SizeChart;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Color;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Size;
import com.Ishwarjit.Wolf_OVRN_backend.exception.ResourceNotFoundException;
import com.Ishwarjit.Wolf_OVRN_backend.repository.CategoryRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.ColorRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.FitRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.ProductImageRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.ProductRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.SizeRepository;
import com.Ishwarjit.Wolf_OVRN_backend.util.SlugUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final SizeChartService sizeChartService;
    private final CloudinaryService cloudinaryService;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final FitRepository fitRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductImageRepository productImageRepository,
            SizeChartService sizeChartService,
            CloudinaryService cloudinaryService,
            ColorRepository colorRepository,
            SizeRepository sizeRepository,
            FitRepository fitRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.sizeChartService = sizeChartService;
        this.cloudinaryService = cloudinaryService;
        this.colorRepository = colorRepository;
        this.sizeRepository = sizeRepository;
        this.fitRepository = fitRepository;
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> list(
            String search, List<String> categories, List<String> sizes, List<String> colors, List<String> fits, Boolean isPremium,
            java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Pageable pageable) {
        Specification<Product> spec = buildSpecification(search, categories, sizes, colors, fits, isPremium, minPrice, maxPrice);
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

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getRelatedProducts(UUID productId) {
        Product target = productRepository.findById(productId).orElse(null);
        if (target == null) {
            return List.of();
        }

        Pageable limit = PageRequest.of(0, 4);
        
        Specification<Product> relatedSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.notEqual(root.get("id"), productId));
            predicates.add(cb.equal(root.get("isActive"), true));
            predicates.add(cb.equal(root.get("inStock"), true));
            
            List<Predicate> orPredicates = new ArrayList<>();
            
            // 1. Same Fit
            if (target.getFit() != null) {
                orPredicates.add(cb.equal(root.get("fit"), target.getFit()));
            }
            
            // 2. Same Categories
            if (target.getCategories() != null && !target.getCategories().isEmpty()) {
                Join<Object, Object> categoryJoin = root.join("categories");
                List<UUID> catIds = target.getCategories().stream().map(com.Ishwarjit.Wolf_OVRN_backend.entity.Category::getId).toList();
                orPredicates.add(categoryJoin.get("id").in(catIds));
            }
            
            // 3. Similar Name (match first word of name)
            if (target.getName() != null) {
                String[] words = target.getName().split("\\s+");
                if (words.length > 0 && words[0].length() > 2) {
                    orPredicates.add(cb.like(cb.lower(root.get("name")), "%" + words[0].toLowerCase() + "%"));
                }
            }
            
            if (!orPredicates.isEmpty()) {
                predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));
            }
            
            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> relatedPage = productRepository.findAll(relatedSpec, limit);
        
        // Fallback: If no related products found, get latest active products
        if (relatedPage.isEmpty()) {
            Specification<Product> fallbackSpec = (root, query, cb) -> {
                query.orderBy(cb.desc(root.get("createdAt")));
                return cb.and(
                    cb.notEqual(root.get("id"), productId),
                    cb.equal(root.get("isActive"), true),
                    cb.equal(root.get("inStock"), true)
                );
            };
            relatedPage = productRepository.findAll(fallbackSpec, limit);
        }

        return relatedPage.map(product -> {
            List<ProductImage> images = productImageRepository
                    .findByProductIdOrderByDisplayOrderAsc(product.getId());
            return ProductSummaryResponse.from(product, images);
        }).toList();
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Transactional
    public ProductDetailResponse create(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setSlug(SlugUtils.generate(request.getName()));
        product.setDescription(request.getDescription());
        product.setSellingPrice(request.getSellingPrice());
        product.setMarkedPrice(request.getMarkedPrice());
        product.setInStock(request.getInStock());
        product.setIsActive(true);
        product.setIsPremium(Boolean.TRUE.equals(request.getIsPremium()));

        if (request.getColorIds() != null && !request.getColorIds().isEmpty()) {
            List<Color> colors = colorRepository.findAllById(request.getColorIds());
            if (colors.size() != request.getColorIds().size()) {
                throw new ResourceNotFoundException("One or more colors not found");
            }
            product.setColors(colors);
        }

        if (request.getSizeIds() != null && !request.getSizeIds().isEmpty()) {
            List<Size> sizes = sizeRepository.findAllById(request.getSizeIds());
            if (sizes.size() != request.getSizeIds().size()) {
                throw new ResourceNotFoundException("One or more sizes not found");
            }
            product.setSizes(sizes);
        }

        validatePrices(request.getSellingPrice(), request.getMarkedPrice());

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            if (categories.size() != request.getCategoryIds().size()) {
                throw new ResourceNotFoundException("One or more categories not found");
            }
            product.setCategories(categories);
        }

        // Link global size chart if provided
        if (request.getSizeChartId() != null) {
            SizeChart chart = sizeChartService.getEntityOrThrow(request.getSizeChartId());
            product.setSizeChart(chart);
        }

        if (request.getFitId() != null) {
            com.Ishwarjit.Wolf_OVRN_backend.entity.Fit fit = fitRepository.findById(request.getFitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fit not found"));
            product.setFit(fit);
        }

        Product saved = productRepository.save(product);

        List<ProductImage> savedImages = new ArrayList<>();
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (ProductImageRequest imgReq : request.getImages()) {
                ProductImage image = buildImageEntity(saved, imgReq);
                savedImages.add(productImageRepository.save(image));
            }
        }

        return ProductDetailResponse.from(saved, savedImages);
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Transactional
    public ProductDetailResponse update(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        if (request.getName() != null)        product.setName(request.getName());
        if (request.getSlug() != null)        product.setSlug(SlugUtils.generate(request.getSlug()));
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getSellingPrice() != null) product.setSellingPrice(request.getSellingPrice());
        if (request.getMarkedPrice() != null)  product.setMarkedPrice(request.getMarkedPrice());
        if (request.getInStock() != null)      product.setInStock(request.getInStock());
        if (request.getIsActive() != null)     product.setIsActive(request.getIsActive());
        if (request.getIsPremium() != null)    product.setIsPremium(request.getIsPremium());

        if (request.getColorIds() != null) {
            List<Color> colors = colorRepository.findAllById(request.getColorIds());
            if (colors.size() != request.getColorIds().size()) {
                throw new ResourceNotFoundException("One or more colors not found");
            }
            product.setColors(colors);
        }

        if (request.getSizeIds() != null) {
            List<Size> sizes = sizeRepository.findAllById(request.getSizeIds());
            if (sizes.size() != request.getSizeIds().size()) {
                throw new ResourceNotFoundException("One or more sizes not found");
            }
            product.setSizes(sizes);
        }

        validatePrices(
                request.getSellingPrice() != null ? request.getSellingPrice() : product.getSellingPrice(),
                request.getMarkedPrice()  != null ? request.getMarkedPrice()  : product.getMarkedPrice());

        if (request.getCategoryIds() != null) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            if (categories.size() != request.getCategoryIds().size()) {
                throw new ResourceNotFoundException("One or more categories not found");
            }
            product.setCategories(categories);
        }

        // Size chart link: set, clear, or leave unchanged
        if (Boolean.TRUE.equals(request.getClearSizeChart())) {
            product.setSizeChart(null);
        } else if (request.getSizeChartId() != null) {
            SizeChart chart = sizeChartService.getEntityOrThrow(request.getSizeChartId());
            product.setSizeChart(chart);
        }

        // Fit link: set, clear, or leave unchanged
        if (Boolean.TRUE.equals(request.getClearFit())) {
            product.setFit(null);
        } else if (request.getFitId() != null) {
            com.Ishwarjit.Wolf_OVRN_backend.entity.Fit fit = fitRepository.findById(request.getFitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fit not found"));
            product.setFit(fit);
        }

        Product saved = productRepository.save(product);

        if (request.getImages() != null) {
            productImageRepository.deleteByProductId(saved.getId());
            for (ProductImageRequest imgReq : request.getImages()) {
                productImageRepository.save(buildImageEntity(saved, imgReq));
            }
        }

        List<ProductImage> images = productImageRepository
                .findByProductIdOrderByDisplayOrderAsc(saved.getId());
        return ProductDetailResponse.from(saved, images);
    }

    // -------------------------------------------------------------------------
    // Delete product
    // -------------------------------------------------------------------------

    @Transactional
    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Image management
    // -------------------------------------------------------------------------

    @Transactional
    public ImageUploadResponse addImage(UUID productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        String secureUrl = cloudinaryService.upload(file);
        long existingCount = productImageRepository.countByProductId(productId);

        String originalName = file.getOriginalFilename();
        String imageName = originalName != null ? originalName : "product-image";
        String slug = buildUniqueImageSlug(SlugUtils.generate(stripExtension(imageName)));

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl(secureUrl);
        image.setImageName(imageName);
        image.setSlug(slug);
        image.setAltText(product.getName());
        image.setIsPrimary(existingCount == 0);
        image.setDisplayOrder((int) existingCount);

        return ImageUploadResponse.from(productImageRepository.save(image));
    }

    @Transactional
    public void deleteImage(UUID imageId) {
        if (!productImageRepository.existsById(imageId)) {
            throw new ResourceNotFoundException("Product image not found: " + imageId);
        }
        productImageRepository.deleteById(imageId);
    }

    @Transactional
    public ProductImageResponse updateImage(UUID imageId, UpdateImageRequest request) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found: " + imageId));
        if (request.getAltText() != null)     image.setAltText(request.getAltText());
        if (request.getIsPrimary() != null)   image.setIsPrimary(request.getIsPrimary());
        if (request.getDisplayOrder() != null) image.setDisplayOrder(request.getDisplayOrder());
        return ProductImageResponse.from(productImageRepository.save(image));
    }

    // -------------------------------------------------------------------------
    // Inner DTO for updateImage
    // -------------------------------------------------------------------------

    @lombok.Getter
    @lombok.Setter
    public static class UpdateImageRequest {
        private String altText;
        private Boolean isPrimary;
        private Integer displayOrder;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static void validatePrices(java.math.BigDecimal selling, java.math.BigDecimal marked) {
        if (selling != null && selling.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Selling price cannot be less than 0.");
        }
        if (marked != null) {
            if (marked.compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Marked price cannot be less than 0.");
            }
            if (selling != null && marked.compareTo(selling) <= 0) {
                throw new IllegalArgumentException("Marked price must be strictly greater than selling price.");
            }
        }
    }

    private ProductImage buildImageEntity(Product product, ProductImageRequest req) {
        ProductImage img = new ProductImage();
        img.setProduct(product);
        img.setUrl(req.getUrl());
        img.setAltText(req.getAltText() != null ? req.getAltText() : product.getName());
        img.setIsPrimary(Boolean.TRUE.equals(req.getIsPrimary()));
        img.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0);
        return img;
    }

    private static String stripExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx > 0 ? filename.substring(0, idx) : filename;
    }

    private String buildUniqueImageSlug(String base) {
        String candidate = base;
        for (int i = 0; i < 5; i++) {
            if (!productImageRepository.existsBySlug(candidate)) {
                return candidate;
            }
            candidate = base + "-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return base + "-" + UUID.randomUUID();
    }

    private Specification<Product> buildSpecification(
            String search, List<String> categories, List<String> sizes, List<String> colors, List<String> fits, Boolean isPremium,
            java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String[] terms = search.toLowerCase().trim().split("\\s+");
                Predicate[] termPredicates = new Predicate[terms.length];
                for (int i = 0; i < terms.length; i++) {
                    termPredicates[i] = cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + terms[i] + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + terms[i] + "%"));
                }
                predicates.add(cb.or(termPredicates));
            }
            if (categories != null && !categories.isEmpty()) {
                Join<Product, Category> join = root.join("categories");
                List<Predicate> catPredicates = new ArrayList<>();
                for (String cat : categories) {
                    try {
                        UUID id = UUID.fromString(cat);
                        catPredicates.add(cb.equal(join.get("id"), id));
                    } catch (IllegalArgumentException e) {
                        catPredicates.add(cb.equal(join.get("slug"), cat));
                    }
                }
                predicates.add(cb.or(catPredicates.toArray(new Predicate[0])));
            }
            if (sizes != null && !sizes.isEmpty()) {
                Join<Product, Size> join = root.join("sizes");
                List<Predicate> sizePredicates = new ArrayList<>();
                for (String size : sizes) {
                    try {
                        UUID id = UUID.fromString(size);
                        sizePredicates.add(cb.equal(join.get("id"), id));
                    } catch (IllegalArgumentException e) {
                        sizePredicates.add(cb.equal(cb.lower(join.get("sizeName")), size.toLowerCase()));
                    }
                }
                predicates.add(cb.or(sizePredicates.toArray(new Predicate[0])));
            }
            if (colors != null && !colors.isEmpty()) {
                Join<Product, Color> join = root.join("colors");
                List<Predicate> colorPredicates = new ArrayList<>();
                for (String color : colors) {
                    try {
                        UUID id = UUID.fromString(color);
                        colorPredicates.add(cb.equal(join.get("id"), id));
                    } catch (IllegalArgumentException e) {
                        colorPredicates.add(cb.equal(cb.lower(join.get("colorName")), color.toLowerCase()));
                    }
                }
                predicates.add(cb.or(colorPredicates.toArray(new Predicate[0])));
            }
            if (fits != null && !fits.isEmpty()) {
                Join<Product, com.Ishwarjit.Wolf_OVRN_backend.entity.Fit> join = root.join("fit");
                List<Predicate> fitPredicates = new ArrayList<>();
                for (String fit : fits) {
                    try {
                        UUID id = UUID.fromString(fit);
                        fitPredicates.add(cb.equal(join.get("id"), id));
                    } catch (IllegalArgumentException e) {
                        fitPredicates.add(cb.equal(cb.lower(join.get("name")), fit.toLowerCase()));
                    }
                }
                predicates.add(cb.or(fitPredicates.toArray(new Predicate[0])));
            }
            if (isPremium != null) predicates.add(cb.equal(root.get("isPremium"), isPremium));
            if (minPrice != null)  predicates.add(cb.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
            if (maxPrice != null)  predicates.add(cb.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
            
            query.distinct(true);
            
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
