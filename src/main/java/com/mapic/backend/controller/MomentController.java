package com.mapic.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapic.backend.dto.request.CreateMomentRequest;
import com.mapic.backend.dto.response.MomentResponse;
import com.mapic.backend.dto.response.PageResponse;
import com.mapic.backend.entity.Moment;
import com.mapic.backend.entity.User;
import com.mapic.backend.repository.UserRepository;
import com.mapic.backend.service.IMomentService;
import com.mapic.backend.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/moments")
@RequiredArgsConstructor
@Slf4j
public class MomentController {

    private final IMomentService momentService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<MomentResponse>> createMoment(
            @RequestPart("files") MultipartFile[] files,
            @RequestPart("metadata") String metadataJson) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Received request to create moment from user: {} with {} files", username, files.length);
        log.info("Metadata JSON: {}", metadataJson);

        try {
            // Parse JSON string to CreateMomentRequest
            CreateMomentRequest metadata = objectMapper.readValue(metadataJson, CreateMomentRequest.class);
            
            Moment moment = momentService.createMoment(author, Arrays.asList(files), metadata);
            MomentResponse response = MomentResponse.fromEntity(moment);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Moment created successfully", response));
        } catch (Exception e) {
            log.error("Error creating moment", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create moment: " + e.getMessage()));
        }
    }

    @GetMapping("/my-moments")
    public ResponseEntity<ApiResponse<List<MomentResponse>>> getMyMoments() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Moment> moments = momentService.getMomentsByAuthor(author.getId());
        List<MomentResponse> responses = moments.stream()
                .map(MomentResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Fetched my moments", responses));
    }

    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<PageResponse<MomentResponse>>> getFeedMoments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Fetching feed for user: {} (page: {}, size: {})", username, page, size);

        // Validate page size (max 50)
        if (size > 50) {
            size = 50;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Moment> momentsPage = momentService.getFeedMoments(user.getId(), pageable);
        
        List<MomentResponse> responses = momentsPage.getContent().stream()
                .map(MomentResponse::fromEntity)
                .collect(Collectors.toList());

        PageResponse<MomentResponse> pageResponse = PageResponse.<MomentResponse>builder()
                .content(responses)
                .pageNumber(momentsPage.getNumber())
                .pageSize(momentsPage.getSize())
                .totalElements(momentsPage.getTotalElements())
                .totalPages(momentsPage.getTotalPages())
                .first(momentsPage.isFirst())
                .last(momentsPage.isLast())
                .hasNext(momentsPage.hasNext())
                .hasPrevious(momentsPage.hasPrevious())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Fetched feed moments", pageResponse));
    }

    @GetMapping("/explore")
    public ResponseEntity<ApiResponse<PageResponse<MomentResponse>>> exploreMoments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String provinceId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "newest") String sort) {
        
        log.info("Exploring moments (page: {}, size: {}, province: {}, category: {}, sort: {})", 
                 page, size, provinceId, category, sort);

        // Validate page size (max 50)
        if (size > 50) {
            size = 50;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Moment> momentsPage = momentService.exploreMoments(provinceId, category, sort, pageable);
        
        List<MomentResponse> responses = momentsPage.getContent().stream()
                .map(MomentResponse::fromEntity)
                .collect(Collectors.toList());

        PageResponse<MomentResponse> pageResponse = PageResponse.<MomentResponse>builder()
                .content(responses)
                .pageNumber(momentsPage.getNumber())
                .pageSize(momentsPage.getSize())
                .totalElements(momentsPage.getTotalElements())
                .totalPages(momentsPage.getTotalPages())
                .first(momentsPage.isFirst())
                .last(momentsPage.isLast())
                .hasNext(momentsPage.hasNext())
                .hasPrevious(momentsPage.hasPrevious())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Explored moments", pageResponse));
    }
}
