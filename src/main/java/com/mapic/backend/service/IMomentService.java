package com.mapic.backend.service;

import com.mapic.backend.dto.MomentDto;
import com.mapic.backend.dto.request.CreateMomentRequest;
import com.mapic.backend.entity.Moment;
import com.mapic.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IMomentService {
    Moment createMoment(User author, List<MultipartFile> files, CreateMomentRequest request);
    List<Moment> getMomentsByAuthor(Long authorId);
    List<Moment> getMomentsByUser(Long userId, Long currentUserId);
    Page<Moment> getFeedMoments(Long userId, Pageable pageable);
    Page<Moment> exploreMoments(Integer provinceId, String category, String sort, Pageable pageable);
    MomentDto convertToDto(Moment moment, Long currentUserId);
    Moment getMomentById(Long momentId);
    void deleteMoment(Long momentId, Long userId);
    Moment updateMomentContent(Long momentId, Long userId, String newContent);
}
