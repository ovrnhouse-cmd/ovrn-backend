package com.Ishwarjit.Wolf_OVRN_backend.controller;

import com.Ishwarjit.Wolf_OVRN_backend.dto.ApiResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.FaqRequest;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Faq;
import com.Ishwarjit.Wolf_OVRN_backend.exception.ResourceNotFoundException;
import com.Ishwarjit.Wolf_OVRN_backend.repository.FaqRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/faqs")
public class FaqController {

    private final FaqRepository faqRepository;

    public FaqController(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Faq>>> getAllFaqs(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int limit) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(limit, 1));
        return ResponseEntity.ok(ApiResponse.ok(faqRepository.findAll(pageable)));
    }

    @GetMapping("/{category}")
    public ResponseEntity<ApiResponse<Page<Faq>>> getFaqsByCategory(
            @PathVariable com.Ishwarjit.Wolf_OVRN_backend.entity.FaqCategory category,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int limit) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(limit, 1));
        return ResponseEntity.ok(ApiResponse.ok(faqRepository.findByCategory(category, pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Faq>> createFaq(@Valid @RequestBody FaqRequest request) {
        Faq faq = new Faq();
        faq.setCategory(request.getCategory());
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        
        Faq savedFaq = faqRepository.save(faq);
        return ResponseEntity.ok(ApiResponse.ok(savedFaq, "FAQ created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Faq>> updateFaq(
            @PathVariable UUID id,
            @Valid @RequestBody FaqRequest request) {
        
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found"));
                
        faq.setCategory(request.getCategory());
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        
        Faq updatedFaq = faqRepository.save(faq);
        return ResponseEntity.ok(ApiResponse.ok(updatedFaq, "FAQ updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFaq(@PathVariable UUID id) {
        if (!faqRepository.existsById(id)) {
            throw new ResourceNotFoundException("FAQ not found");
        }
        
        faqRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "FAQ deleted successfully"));
    }
}
