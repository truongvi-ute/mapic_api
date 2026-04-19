package com.mapic.backend.service;

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
    private final IStorageService storageService;
    private final OpenCageService openCageService;

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
}
