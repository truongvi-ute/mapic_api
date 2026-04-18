package com.mapic.backend.service;

import com.mapic.backend.dto.external.OpenCageGeocodeResponse;
import com.mapic.backend.entity.Commune;
import com.mapic.backend.entity.District;
import com.mapic.backend.entity.Province;
import com.mapic.backend.repository.CommuneRepository;
import com.mapic.backend.repository.DistrictRepository;
import com.mapic.backend.repository.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenCageService {

    @Value("${opencage.api.key}")
    private String apiKey;

    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final CommuneRepository communeRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String OPENCAGE_REVERSE_URL = "https://api.opencagedata.com/geocode/v1/json?q=%f+%f&key=%s&language=vi";

    public ResolvedLocation resolveLocation(double lat, double lng) {
        if (apiKey == null || apiKey.equals("YOUR_OPENCAGE_API_KEY_HERE")) {
            log.warn("OpenCage API Key not configured. Skipping reverse geocoding.");
            return null;
        }

        try {
            String url = String.format(OPENCAGE_REVERSE_URL, lat, lng, apiKey);
            OpenCageGeocodeResponse response = restTemplate.getForObject(url, OpenCageGeocodeResponse.class);

            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                return null;
            }

            var components = response.getResults().get(0).getComponents();
            
            // Fallback chain for Vietnam components
            String provinceName = Optional.ofNullable(components.getState()).orElse(components.getCity());
            String districtName = Optional.ofNullable(components.getCityDistrict())
                    .orElse(Optional.ofNullable(components.getCounty()).orElse(components.getDistrict()));
            String communeName = Optional.ofNullable(components.getSuburb())
                    .orElse(Optional.ofNullable(components.getVillage())
                            .orElse(Optional.ofNullable(components.getTown()).orElse(components.getQuarter())));

            ResolvedLocation resolved = new ResolvedLocation();
            resolved.setAddress(response.getResults().get(0).getFormatted());

            if (provinceName == null) return resolved;

            // Name matching logic
            final String finalProvinceName = provinceName;
            provinceRepository.findAll().stream()
                    .filter(p -> finalProvinceName.contains(p.getName()) || p.getName().contains(finalProvinceName))
                    .findFirst()
                    .ifPresent(p -> {
                        resolved.setProvince(p);
                        if (districtName == null) return;
                        
                        final String finalDistrictName = districtName;
                        districtRepository.findByProvinceId(p.getId()).stream()
                                .filter(d -> finalDistrictName.contains(d.getName()) || d.getName().contains(finalDistrictName))
                                .findFirst()
                                .ifPresent(d -> {
                                    resolved.setDistrict(d);
                                    if (communeName == null) return;
                                    
                                    final String finalCommuneName = communeName;
                                    communeRepository.findByDistrictId(d.getId()).stream()
                                            .filter(c -> finalCommuneName.contains(c.getName()) || c.getName().contains(finalCommuneName))
                                            .findFirst()
                                            .ifPresent(resolved::setCommune);
                                });
                    });

            return resolved;
        } catch (Exception e) {
            log.error("Error calling OpenCage API: {}", e.getMessage());
            return null;
        }
    }

    @lombok.Data
    public static class ResolvedLocation {
        private Province province;
        private District district;
        private Commune commune;
        private String address;
    }
}
