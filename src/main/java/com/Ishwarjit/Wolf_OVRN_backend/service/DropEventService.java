package com.Ishwarjit.Wolf_OVRN_backend.service;

import com.Ishwarjit.Wolf_OVRN_backend.dto.CreateDropEventRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.DropEventResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.DropEventSummaryResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.UpdateDropEventRequest;
import com.Ishwarjit.Wolf_OVRN_backend.entity.DropEvent;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Product;
import com.Ishwarjit.Wolf_OVRN_backend.entity.ProductImage;
import com.Ishwarjit.Wolf_OVRN_backend.exception.ResourceNotFoundException;
import com.Ishwarjit.Wolf_OVRN_backend.repository.DropEventRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.ProductImageRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.ProductRepository;
import com.Ishwarjit.Wolf_OVRN_backend.util.SlugUtils;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DropEventService {

    private final DropEventRepository dropEventRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public DropEventService(DropEventRepository dropEventRepository, ProductRepository productRepository, ProductImageRepository productImageRepository) {
        this.dropEventRepository = dropEventRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<DropEventSummaryResponse> list(org.springframework.data.domain.Pageable pageable) {
        return dropEventRepository.findAll(pageable)
                .map(DropEventSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public DropEventResponse getNextUpcomingDrop() {
        return dropEventRepository.findFirstByIsActiveTrueAndDropDateAfterOrderByDropDateAsc(OffsetDateTime.now())
                .map(event -> {
                    if (OffsetDateTime.now().plusMinutes(5).isAfter(event.getDropDate()) || 
                        OffsetDateTime.now().plusMinutes(5).isEqual(event.getDropDate())) {
                        return buildResponse(event);
                    } else {
                        return DropEventResponse.fromWithoutProducts(event);
                    }
                })
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<DropEventSummaryResponse> getPreviousDrops() {
        return dropEventRepository.findPreviousDrops(OffsetDateTime.now())
                .stream()
                .map(DropEventSummaryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DropEventResponse getBySlug(String slug) {
        DropEvent event = dropEventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Drop Event not found: " + slug));
        return buildResponse(event);
    }

    @Transactional(readOnly = true)
    public DropEventResponse getById(UUID id) {
        DropEvent event = dropEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drop Event not found: " + id));
        return buildResponse(event);
    }

    @Transactional
    public DropEventResponse create(CreateDropEventRequest request) {
        DropEvent event = new DropEvent();
        event.setName(request.getName());
        event.setSlug(SlugUtils.generate(request.getName()));
        event.setDescription(request.getDescription());
        event.setDropDate(request.getDropDate());
        event.setIsActive(Boolean.TRUE.equals(request.getIsActive()));

        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            List<Product> products = productRepository.findAllById(request.getProductIds());
            if (products.size() != request.getProductIds().size()) {
                throw new ResourceNotFoundException("One or more products not found");
            }
            event.setProducts(products);
        }

        DropEvent saved = dropEventRepository.save(event);
        return buildResponse(saved);
    }

    @Transactional
    public DropEventResponse update(UUID id, UpdateDropEventRequest request) {
        DropEvent event = dropEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drop Event not found: " + id));

        if (request.getName() != null) {
            event.setName(request.getName());
        }
        if (request.getSlug() != null) {
            event.setSlug(SlugUtils.generate(request.getSlug()));
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getDropDate() != null) {
            event.setDropDate(request.getDropDate());
        }
        if (request.getIsActive() != null) {
            event.setIsActive(request.getIsActive());
        }

        if (request.getProductIds() != null) {
            List<Product> products = productRepository.findAllById(request.getProductIds());
            if (products.size() != request.getProductIds().size()) {
                throw new ResourceNotFoundException("One or more products not found");
            }
            event.setProducts(products);
        }

        DropEvent saved = dropEventRepository.save(event);
        return buildResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        if (!dropEventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Drop Event not found: " + id);
        }
        dropEventRepository.deleteById(id);
    }

    private DropEventResponse buildResponse(DropEvent event) {
        List<UUID> productIds = event.getProducts().stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        Map<UUID, List<ProductImage>> imagesMap = Map.of();
        if (!productIds.isEmpty()) {
            List<ProductImage> allImages = productImageRepository.findByProductIdInOrderByDisplayOrderAsc(productIds);
            imagesMap = allImages.stream()
                    .collect(Collectors.groupingBy(img -> img.getProduct().getId()));
        }

        return DropEventResponse.from(event, imagesMap);
    }
}
