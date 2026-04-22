package com.mapic.backend.controller;

import com.mapic.backend.dto.AlbumDto;
import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.dto.CreateAlbumRequest;
import com.mapic.backend.entity.User;
import com.mapic.backend.repository.UserRepository;
import com.mapic.backend.service.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlbumDto>>> getMyAlbums(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            System.out.println("[AlbumController] Getting albums for user: " + user.getId());
            List<AlbumDto> albums = albumService.getUserAlbums(user.getId());
            System.out.println("[AlbumController] Found " + albums.size() + " albums");
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách album thành công", albums));
        } catch (Exception e) {
            System.err.println("[AlbumController] Error getting albums: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AlbumDto>> createAlbum(
            @Valid @RequestBody CreateAlbumRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            AlbumDto albumDto = albumService.createAlbum(user.getId(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Tạo album thành công", albumDto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi khi tạo album", null));
        }
    }

    @GetMapping("/{albumId}")
    public ResponseEntity<ApiResponse<AlbumDto>> getAlbumDetails(
            @PathVariable Long albumId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            AlbumDto albumDto = albumService.getAlbumDetails(albumId, user.getId());
            return ResponseEntity.ok(new ApiResponse<>(true, "Chi tiết album", albumDto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi khi lấy chi tiết album", null));
        }
    }

    @PutMapping("/{albumId}")
    public ResponseEntity<ApiResponse<AlbumDto>> updateAlbum(
            @PathVariable Long albumId,
            @Valid @RequestBody CreateAlbumRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            AlbumDto albumDto = albumService.updateAlbum(albumId, user.getId(), request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật album thành công", albumDto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi cập nhật album", null));
        }
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<ApiResponse<Void>> deleteAlbum(
            @PathVariable Long albumId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            albumService.deleteAlbum(albumId, user.getId());
            return ResponseEntity.ok(new ApiResponse<>(true, "Xóa album thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi khi xóa album", null));
        }
    }

    @PostMapping("/{albumId}/moments/{momentId}")
    public ResponseEntity<ApiResponse<AlbumDto>> addMomentToAlbum(
            @PathVariable Long albumId,
            @PathVariable Long momentId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            AlbumDto albumDto = albumService.addMomentToAlbum(albumId, momentId, user.getId());
            return ResponseEntity.ok(new ApiResponse<>(true, "Thêm moment vào album thành công", albumDto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi khi thêm moment", null));
        }
    }

    @DeleteMapping("/{albumId}/moments/{momentId}")
    public ResponseEntity<ApiResponse<AlbumDto>> removeMomentFromAlbum(
            @PathVariable Long albumId,
            @PathVariable Long momentId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            AlbumDto albumDto = albumService.removeMomentFromAlbum(albumId, momentId, user.getId());
            return ResponseEntity.ok(new ApiResponse<>(true, "Bỏ lưu moment từ album thành công", albumDto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi bỏ lưu moment khỏi album", null));
        }
    }
}
