package com.mapic.backend.service;

import com.mapic.backend.dto.CommentDto;
import com.mapic.backend.dto.request.CommentRequest;

import java.util.List;

public interface ICommentService {
    CommentDto createComment(Long momentId, CommentRequest request);
    List<CommentDto> getCommentsByMoment(Long momentId);
    void deleteComment(Long commentId);
}
