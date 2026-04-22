package com.mapic.backend.service;

import com.mapic.backend.dto.AlbumDto;
import com.mapic.backend.dto.CreateAlbumRequest;
import com.mapic.backend.dto.MomentDto;
import com.mapic.backend.entity.Album;
import com.mapic.backend.entity.AlbumItem;
import com.mapic.backend.entity.Moment;
import com.mapic.backend.entity.User;
import com.mapic.backend.repository.AlbumItemRepository;
import com.mapic.backend.repository.AlbumRepository;
import com.mapic.backend.repository.MomentRepository;
import com.mapic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumItemRepository albumItemRepository;
    private final UserRepository userRepository;
    private final MomentRepository momentRepository;
    private final IMomentService momentService;

    public List<AlbumDto> getUserAlbums(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Album> albums = albumRepository.findByAuthorOrderByCreatedAtDesc(user);

        return albums.stream()
                .map(this::convertToDtoWithoutMoments)
                .collect(Collectors.toList());
    }

    public AlbumDto getAlbumDetails(Long albumId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Album album = albumRepository.findByIdAndAuthor(albumId, user)
                .orElseThrow(() -> new RuntimeException("Album not found or you don't have permission"));

        // Sort by sortOrder ASC (persistent order from DB)
        List<AlbumItem> items = albumItemRepository.findByAlbumOrderBySortOrderAsc(album);
        
        List<MomentDto> momentDtos = items.stream()
                .map(item -> momentService.convertToDto(item.getMoment(), userId))
                .collect(Collectors.toList());

        AlbumDto dto = convertToDtoWithoutMoments(album);
        dto.setMoments(momentDtos);
        
        return dto;
    }

    @Transactional
    public AlbumDto createAlbum(Long userId, CreateAlbumRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (albumRepository.existsByTitleAndAuthor(request.getTitle(), user)) {
            throw new RuntimeException("Album name already exists");
        }

        Album album = Album.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .author(user)
                .isPrivate(false)
                .build();

        Album savedAlbum = albumRepository.save(album);
        return convertToDtoWithoutMoments(savedAlbum);
    }

    @Transactional
    public AlbumDto updateAlbum(Long albumId, Long userId, CreateAlbumRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Album album = albumRepository.findByIdAndAuthor(albumId, user)
                .orElseThrow(() -> new RuntimeException("Album not found or permission denied"));

        if (!album.getTitle().equals(request.getTitle()) && 
            albumRepository.existsByTitleAndAuthor(request.getTitle(), user)) {
            throw new RuntimeException("Album name already exists");
        }

        album.setTitle(request.getTitle());
        album.setDescription(request.getDescription());

        Album updatedAlbum = albumRepository.save(album);
        return convertToDtoWithoutMoments(updatedAlbum);
    }

    @Transactional
    public void deleteAlbum(Long albumId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Album album = albumRepository.findByIdAndAuthor(albumId, user)
                .orElseThrow(() -> new RuntimeException("Album not found or permission denied"));

        albumItemRepository.deleteByAlbum(album);
        albumRepository.delete(album);
    }

    @Transactional
    public AlbumDto addMomentToAlbum(Long albumId, Long momentId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Album album = albumRepository.findByIdAndAuthor(albumId, user)
                .orElseThrow(() -> new RuntimeException("Album not found or permission denied"));

        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));

        if (albumItemRepository.existsByAlbumAndMoment(album, moment)) {
            throw new RuntimeException("Moment is already in this album");
        }

        // Auto-assign next sortOrder (max + 1), appends to end of album
        Integer maxOrder = albumItemRepository.findMaxSortOrderByAlbum(album);
        int nextOrder = (maxOrder == null ? -1 : maxOrder) + 1;

        AlbumItem albumItem = AlbumItem.builder()
                .album(album)
                .moment(moment)
                .sortOrder(nextOrder)
                .build();
        
        albumItemRepository.save(albumItem);

        return convertToDtoWithoutMoments(album);
    }

    @Transactional
    public AlbumDto removeMomentFromAlbum(Long albumId, Long momentId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Album album = albumRepository.findByIdAndAuthor(albumId, user)
                .orElseThrow(() -> new RuntimeException("Album not found or permission denied"));

        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));

        AlbumItem albumItem = albumItemRepository.findByAlbumAndMoment(album, moment)
                .orElseThrow(() -> new RuntimeException("Moment is not in this album"));

        albumItemRepository.delete(albumItem);

        return convertToDtoWithoutMoments(album);
    }

    @Transactional
    public AlbumDto reorderAlbumItem(Long albumId, Long momentId, String direction, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Album album = albumRepository.findByIdAndAuthor(albumId, user)
                .orElseThrow(() -> new RuntimeException("Album not found or permission denied"));

        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));

        // Fetch all items sorted by sortOrder
        List<AlbumItem> items = albumItemRepository.findByAlbumOrderBySortOrderAsc(album);
        int currentIndex = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getMoment().getId().equals(momentId)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) {
            throw new RuntimeException("Moment is not in this album");
        }

        int targetIndex = currentIndex;
        if ("left".equalsIgnoreCase(direction) && currentIndex > 0) {
            targetIndex = currentIndex - 1;
        } else if ("right".equalsIgnoreCase(direction) && currentIndex < items.size() - 1) {
            targetIndex = currentIndex + 1;
        }

        if (targetIndex != currentIndex) {
            AlbumItem current = items.get(currentIndex);
            AlbumItem target = items.get(targetIndex);

            // Swap sortOrder values
            int tempOrder = current.getSortOrder();
            current.setSortOrder(target.getSortOrder());
            target.setSortOrder(tempOrder);

            albumItemRepository.save(current);
            albumItemRepository.save(target);
        }

        return getAlbumDetails(albumId, userId);
    }

    /**
     * Copy a shared album (from any user) into the current user's albums.
     * Creates a new album with same title/description and adds all moments from the source.
     */
    @Transactional
    public AlbumDto saveSharedAlbum(Long sourceAlbumId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Load source album — any album is accessible for saving (it was shared)
        Album source = albumRepository.findById(sourceAlbumId)
                .orElseThrow(() -> new RuntimeException("Album not found"));

        // Don't let user save their own album
        if (source.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Đây là album của bạn");
        }

        // Build a unique title (append copy suffix if name already exists)
        String baseTitle = source.getTitle();
        String title = baseTitle;
        int suffix = 1;
        while (albumRepository.existsByTitleAndAuthor(title, user)) {
            title = baseTitle + " (" + suffix++ + ")";
        }

        Album newAlbum = Album.builder()
                .title(title)
                .description(source.getDescription())
                .author(user)
                .isPrivate(false)
                .build();
        newAlbum = albumRepository.save(newAlbum);

        // Copy all moments from source album
        List<AlbumItem> sourceItems = albumItemRepository.findByAlbumOrderBySortOrderAsc(source);
        for (AlbumItem item : sourceItems) {
            AlbumItem copy = AlbumItem.builder()
                    .album(newAlbum)
                    .moment(item.getMoment())
                    .sortOrder(item.getSortOrder())
                    .build();
            albumItemRepository.save(copy);
        }

        return convertToDtoWithoutMoments(newAlbum);
    }

    private AlbumDto convertToDtoWithoutMoments(Album album) {
        long count = albumItemRepository.countByAlbum(album);
        
        return AlbumDto.builder()
                .id(album.getId())
                .title(album.getTitle())
                .description(album.getDescription())
                .coverImageUrl(album.getCoverImageUrl())
                .itemCount(count)
                .createdAt(album.getCreatedAt())
                .build();
    }
}
