package com.mapic.backend.service;

import com.mapic.backend.dto.request.CreateMomentRequest;
import com.mapic.backend.entity.*;
import com.mapic.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        Province province = null;
        District district = null;
        Commune commune = null;

        if (request.getLatitude() != null && request.getLongitude() != null) {
            // Mode 1: GPS Capture
            location = locationRepository.save(Location.builder()
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .address(request.getAddressName())
                    .name("GPS Location")
                    .build());

            // Use OpenCage to resolve administrative regions
            var resolved = openCageService.resolveLocation(request.getLatitude(), request.getLongitude());
            if (resolved != null) {
                province = resolved.getProvince();
                district = resolved.getDistrict();
                commune = resolved.getCommune();
                if (location.getAddress() == null) {
                    location.setAddress(resolved.getAddress());
                    location = locationRepository.save(location);
                }
            }
        } else {
            // Mode 2: Picker selection
            if (request.getProvinceId() != null) {
                province = provinceRepository.findById(request.getProvinceId()).orElse(null);
            }
            if (request.getDistrictId() != null) {
                district = districtRepository.findById(request.getDistrictId()).orElse(null);
            }
            if (request.getCommuneId() != null) {
                commune = communeRepository.findById(request.getCommuneId()).orElse(null);
            }
        }

        // 2. Create Moment
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

        // 3. Save Media
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
            }
        }

        return moment;
    }

    @Override
    public List<Moment> getMomentsByAuthor(Long authorId) {
        return momentRepository.findByAuthorId(authorId);
    }
}
