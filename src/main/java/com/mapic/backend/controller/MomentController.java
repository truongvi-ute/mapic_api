package com.mapic.backend.controller;

import com.mapic.backend.dto.request.CreateMomentRequest;
import com.mapic.backend.entity.Moment;
import com.mapic.backend.entity.User;
import com.mapic.backend.repository.UserRepository;
import com.mapic.backend.service.IMomentService;
import com.mapic.backend.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/moments")
@RequiredArgsConstructor
@Slf4j
public class MomentController {

    private final IMomentService momentService;
    private final UserRepository userRepository;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<Moment>> createMoment(
            @RequestPart("files") MultipartFile[] files,
            @RequestPart("metadata") CreateMomentRequest metadata) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Received request to create moment from user: {} with {} files", username, files.length);

        Moment moment = momentService.createMoment(author, Arrays.asList(files), metadata);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Moment created successfully", moment));
    }

    @GetMapping("/my-moments")
    public ResponseEntity<ApiResponse<List<Moment>>> getMyMoments() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Moment> moments = momentService.getMomentsByAuthor(author.getId());
        return ResponseEntity.ok(ApiResponse.success("Fetched my moments", moments));
    }
}
