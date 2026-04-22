package com.mapic.backend.service;

import com.mapic.backend.dto.request.ReportRequest;
import com.mapic.backend.entity.Report;
import com.mapic.backend.entity.User;
import com.mapic.backend.exception.NotFoundException;
import com.mapic.backend.repository.ReportRepository;
import com.mapic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements IReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void submitReport(ReportRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Report report = Report.builder()
                .reporter(reporter)
                .targetId(request.getTargetId())
                .targetType(request.getTargetType())
                .reason(request.getReason())
                .build();

        reportRepository.save(report);
        
        log.info("User {} submitted a report for {} id: {}, reason: {}", 
                username, request.getTargetType(), request.getTargetId(), request.getReason());
    }
}
