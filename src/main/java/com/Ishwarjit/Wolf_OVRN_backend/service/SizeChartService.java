package com.Ishwarjit.Wolf_OVRN_backend.service;

import com.Ishwarjit.Wolf_OVRN_backend.dto.SizeChartRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.SizeChartResponse;
import com.Ishwarjit.Wolf_OVRN_backend.entity.SizeChart;
import com.Ishwarjit.Wolf_OVRN_backend.exception.ResourceNotFoundException;
import com.Ishwarjit.Wolf_OVRN_backend.repository.SizeChartRepository;
import com.Ishwarjit.Wolf_OVRN_backend.util.SlugUtils;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SizeChartService {

    private final SizeChartRepository sizeChartRepository;
    private final CloudinaryService cloudinaryService;

    public SizeChartService(
            SizeChartRepository sizeChartRepository,
            CloudinaryService cloudinaryService) {
        this.sizeChartRepository = sizeChartRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // -------------------------------------------------------------------------
    // List all (for admin dropdown)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<SizeChartResponse> findAll() {
        return sizeChartRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(SizeChartResponse::from)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Get single
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public SizeChartResponse findById(UUID id) {
        return SizeChartResponse.from(getOrThrow(id));
    }

    // -------------------------------------------------------------------------
    // Create (multipart: file + name)
    // -------------------------------------------------------------------------

    @Transactional
    public SizeChartResponse create(String name, MultipartFile file) throws IOException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Size chart name is required");
        }

        String secureUrl = cloudinaryService.upload(file, "wolf-ovrn/size-charts");

        String originalName = file.getOriginalFilename();
        String imageName = (originalName != null && !originalName.isBlank()) ? originalName : "size-chart";

        String baseSlug = SlugUtils.generate(stripExtension(imageName));
        String slug = buildUniqueSlug(baseSlug);

        SizeChart chart = new SizeChart();
        chart.setName(name.trim());
        chart.setImageName(imageName);
        chart.setSlug(slug);
        chart.setUrl(secureUrl);
        chart.setAltText(name.trim());

        return SizeChartResponse.from(sizeChartRepository.save(chart));
    }

    // -------------------------------------------------------------------------
    // Update (name and/or altText)
    // -------------------------------------------------------------------------

    @Transactional
    public SizeChartResponse update(UUID id, SizeChartRequest request) {
        SizeChart chart = getOrThrow(id);
        if (request.getName() != null && !request.getName().isBlank()) {
            chart.setName(request.getName().trim());
        }
        if (request.getAltText() != null) {
            chart.setAltText(request.getAltText());
        }
        return SizeChartResponse.from(sizeChartRepository.save(chart));
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Transactional
    public void delete(UUID id) {
        if (!sizeChartRepository.existsById(id)) {
            throw new ResourceNotFoundException("Size chart not found: " + id);
        }
        sizeChartRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Package-visible helper so ProductService can resolve the entity
    // -------------------------------------------------------------------------

    public SizeChart getEntityOrThrow(UUID id) {
        return getOrThrow(id);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private SizeChart getOrThrow(UUID id) {
        return sizeChartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Size chart not found: " + id));
    }

    private static String stripExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx > 0 ? filename.substring(0, idx) : filename;
    }

    private String buildUniqueSlug(String base) {
        String candidate = base;
        for (int i = 0; i < 5; i++) {
            if (!sizeChartRepository.existsBySlug(candidate)) {
                return candidate;
            }
            candidate = base + "-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return base + "-" + UUID.randomUUID();
    }
}
