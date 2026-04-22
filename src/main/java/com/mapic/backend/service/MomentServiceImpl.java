package com.mapic.backend.service;

import com.mapic.backend.dto.MomentDto;
import com.mapic.backend.dto.request.CreateMomentRequest;
import com.mapic.backend.entity.*;
import com.mapic.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomentServiceImpl implements IMomentService {

    private final MomentRepository momentRepository;
    private final MomentMediaRepository mediaRepository;
    private final LocationRepository locationRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final CommuneRepository communeRepository;
    private final FriendshipRepository friendshipRepository;
    private final IStorageService storageService;
    private final OpenCageService openCageService;
    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public Moment createMoment(User author, List<MultipartFile> files, CreateMomentRequest request) {
        log.info("Creating moment for user: {}", author.getUsername());

        // 1. Resolve Location
        Location location = null;

        if (request.getLatitude() != null && request.getLongitude() != null) {
            // Lưu location với latitude, longitude và address
            location = locationRepository.save(Location.builder()
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .address(request.getAddressName())
                    .name("User Location")
                    .build());
            
            log.info("Saved location: lat={}, lng={}, address={}", 
                request.getLatitude(), request.getLongitude(), request.getAddressName());
        }

        // 2. Resolve Province, District, Commune
        Province province = null;
        District district = null;
        Commune commune = null;

        if (request.getProvinceId() != null) {
            province = provinceRepository.findById(request.getProvinceId()).orElse(null);
            log.info("Found province: {}", province != null ? province.getName() : "null");
        }

        if (request.getDistrictId() != null) {
            district = districtRepository.findById(request.getDistrictId()).orElse(null);
            log.info("Found district: {}", district != null ? district.getName() : "null");
        }

        if (request.getCommuneId() != null) {
            commune = communeRepository.findById(request.getCommuneId()).orElse(null);
            log.info("Found commune: {}", commune != null ? commune.getName() : "null");
        }

        // 3. Create Moment
        Moment moment = Moment.builder()
                .author(author)
                .content(request.getCaption())
                .location(location)
                .province(province)
                .district(district)
                .commune(commune)
                .category(request.getCategory())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
                .status("ACTIVE")
                .build();

        moment = momentRepository.save(moment);
        log.info("Created moment with id: {}", moment.getId());

        // 4. Save Media
        if (files != null && !files.isEmpty()) {
            int sortOrder = 0;
            for (MultipartFile file : files) {
                String filename = storageService.store(file, "moments");
                MediaType type = file.getContentType() != null && file.getContentType().startsWith("video") 
                        ? MediaType.VIDEO : MediaType.IMAGE;

                MomentMedia media = MomentMedia.builder()
                        .moment(moment)
                        .mediaUrl(filename)
                        .mediaType(type)
                        .sortOrder(sortOrder++)
                        .build();
                
                mediaRepository.save(media);
                log.info("Saved media: {} (type: {})", filename, type);
            }
        }

        return moment;
    }

    @Override
    public List<Moment> getMomentsByAuthor(Long authorId) {
        return momentRepository.findByAuthorId(authorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Moment> getMomentsByUser(Long userId, Long currentUserId) {
        // Check if they are friends
        boolean areFriends = friendshipRepository.existsFriendshipBetweenUsers(currentUserId, userId);
        
        if (areFriends || userId.equals(currentUserId)) {
            // Return all moments if friends or viewing own profile
            return momentRepository.findByAuthorId(userId);
        } else {
            // Return only public moments if not friends
            return momentRepository.findPublicMomentsByAuthorId(userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Moment> getFeedMoments(Long userId, Pageable pageable) {
        log.info("Fetching feed moments for user: {} (page: {}, size: {})", 
                userId, pageable.getPageNumber(), pageable.getPageSize());
        
        // Get paginated moments (without JOIN FETCH to avoid pagination issues)
        Page<Moment> momentsPage = momentRepository.findFeedMoments(userId, pageable);
        
        // Manually fetch relationships for the current page to avoid N+1 queries
        List<Moment> moments = momentsPage.getContent();
        if (!moments.isEmpty()) {
            // Force load lazy relationships
            moments.forEach(moment -> {
                // Load author and profile
                moment.getAuthor().getName();
                if (moment.getAuthor().getUserProfile() != null) {
                    moment.getAuthor().getUserProfile().getAvatarUrl();
                }
                
                // Load location
                if (moment.getLocation() != null) {
                    moment.getLocation().getAddress();
                }
                
                // Load province, district, commune
                if (moment.getProvince() != null) {
                    moment.getProvince().getName();
                }
                if (moment.getDistrict() != null) {
                    moment.getDistrict().getName();
                }
                if (moment.getCommune() != null) {
                    moment.getCommune().getName();
                }
                
                // Load media
                if (moment.getMedia() != null) {
                    moment.getMedia().size();
                }
            });
        }
        
        log.info("Fetched {} moments for feed", moments.size());
        return momentsPage;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Moment> exploreMoments(Integer provinceId, String category, String sort, Pageable pageable) {
        log.info("Exploring moments with filters - province: {}, category: {}, sort: {}", 
                 provinceId, category, sort);
        
        // Note: sort parameter is currently ignored, always returns newest first
        // TODO: Implement like/comment counting to enable popular sorting
        
        return momentRepository.exploreMoments(provinceId, category, pageable);
    }

    @Override
    public Moment getMomentById(Long momentId) {
        return momentRepository.findById(momentId).orElse(null);
    }

    @Override
    public MomentDto convertToDto(Moment moment, Long currentUserId) {
        // Build author DTO
        MomentDto.AuthorDto authorDto = MomentDto.AuthorDto.builder()
                .id(moment.getAuthor().getId())
                .username(moment.getAuthor().getUsername())
                .fullName(moment.getAuthor().getName())
                .avatarUrl(moment.getAuthor().getUserProfile() != null ? 
                    moment.getAuthor().getUserProfile().getAvatarUrl() : null)
                .build();

        // Build location DTO if exists
        MomentDto.LocationDto locationDto = null;
        if (moment.getLocation() != null) {
            locationDto = MomentDto.LocationDto.builder()
                    .id(moment.getLocation().getId())
                    .latitude(moment.getLocation().getLatitude())
                    .longitude(moment.getLocation().getLongitude())
                    .address(moment.getLocation().getAddress())
                    .name(moment.getLocation().getName())
                    .build();
        }

        // Build province DTO if exists
        MomentDto.ProvinceDto provinceDto = null;
        if (moment.getProvince() != null) {
            provinceDto = MomentDto.ProvinceDto.builder()
                    .id(moment.getProvince().getId().longValue())
                    .name(moment.getProvince().getName())
                    .code(moment.getProvince().getCode())
                    .build();
        }

        // Build district DTO if exists
        MomentDto.DistrictDto districtDto = null;
        if (moment.getDistrict() != null) {
            districtDto = MomentDto.DistrictDto.builder()
                    .id(moment.getDistrict().getId().longValue())
                    .name(moment.getDistrict().getName())
                    .code(moment.getDistrict().getCode())
                    .build();
        }

        // Build commune DTO if exists
        MomentDto.CommuneDto communeDto = null;
        if (moment.getCommune() != null) {
            communeDto = MomentDto.CommuneDto.builder()
                    .id(moment.getCommune().getId().longValue())
                    .name(moment.getCommune().getName())
                    .code(moment.getCommune().getCode())
                    .build();
        }

        // Build media DTOs
        List<MomentDto.MediaDto> mediaDtos = moment.getMedia().stream()
                .map(media -> MomentDto.MediaDto.builder()
                        .id(media.getId())
                        .mediaUrl(media.getMediaUrl())
                        .mediaType(media.getMediaType().name())
                        .sortOrder(media.getSortOrder())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        // Count reactions and comments
        long reactionCount = reactionRepository.countByMoment(moment);
        long commentCount = commentRepository.countByMoment(moment);

        // Check if current user reacted
        boolean userReacted = false;
        if (currentUserId != null) {
            User currentUser = new User();
            currentUser.setId(currentUserId);
            userReacted = reactionRepository.existsByUserAndMoment(currentUser, moment);
        }

        // Build and return MomentDto
        return MomentDto.builder()
                .id(moment.getId())
                .content(moment.getContent())
                .author(authorDto)
                .location(locationDto)
                .province(provinceDto)
                .district(districtDto)
                .commune(communeDto)
                .category(moment.getCategory())
                .isPublic(moment.getIsPublic())
                .status(moment.getStatus())
                .createdAt(moment.getCreatedAt())
                .media(mediaDtos)
                .reactionCount(reactionCount)
                .userReacted(userReacted)
                .commentCount(commentCount)
                .build();
    }
}
