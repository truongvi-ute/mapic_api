package com.mapic.backend.service;

import com.mapic.backend.dto.external.ProvinceDTO;
import com.mapic.backend.entity.Commune;
import com.mapic.backend.entity.District;
import com.mapic.backend.entity.Province;
import com.mapic.backend.repository.CommuneRepository;
import com.mapic.backend.repository.DistrictRepository;
import com.mapic.backend.repository.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements ILocationService {

    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final CommuneRepository communeRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_URL = "https://provinces.open-api.vn/api/?depth=3";

    @Override
    public List<Province> getAllProvinces() {
        return provinceRepository.findAll().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<District> getDistrictsByProvince(Integer provinceId) {
        return districtRepository.findByProvinceId(provinceId).stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Commune> getCommunesByDistrict(Integer districtId) {
        return communeRepository.findByDistrictId(districtId).stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void seedLocations() {
        log.info("Starting location seeding from {}", API_URL);
        try {
            ResponseEntity<List<ProvinceDTO>> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ProvinceDTO>>() {}
            );

            List<ProvinceDTO> provinces = response.getBody();
            if (provinces == null) return;

            // Only seed Hanoi (1) and HCM (79) as samples for now
            List<Integer> targetProvinceCodes = List.of(1, 79);

            for (ProvinceDTO pDto : provinces) {
                if (!targetProvinceCodes.contains(pDto.getCode())) continue;

                Province province = provinceRepository.findById(pDto.getCode())
                        .orElseGet(() -> provinceRepository.save(Province.builder()
                                .id(pDto.getCode())
                                .name(pDto.getName())
                                .code(pDto.getCodename())
                                .region(pDto.getDivisionType())
                                .build()));

                if (pDto.getDistricts() != null) {
                    for (var dDto : pDto.getDistricts()) {
                        District district = districtRepository.findById(dDto.getCode())
                                .orElseGet(() -> districtRepository.save(District.builder()
                                        .id(dDto.getCode())
                                        .name(dDto.getName())
                                        .code(dDto.getCodename())
                                        .province(province)
                                        .build()));

                        if (dDto.getWards() != null) {
                            for (var cDto : dDto.getWards()) {
                                if (!communeRepository.existsById(cDto.getCode())) {
                                    communeRepository.save(Commune.builder()
                                            .id(cDto.getCode())
                                            .name(cDto.getName())
                                            .code(cDto.getCodename())
                                            .district(district)
                                            .build());
                                }
                            }
                        }
                    }
                }
            }
            log.info("Location seeding completed for target provinces.");
        } catch (Exception e) {
            log.error("Error seeding locations: {}", e.getMessage());
        }
    }
}
