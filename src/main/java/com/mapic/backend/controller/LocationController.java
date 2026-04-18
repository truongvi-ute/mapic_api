package com.mapic.backend.controller;

import com.mapic.backend.entity.Commune;
import com.mapic.backend.entity.District;
import com.mapic.backend.entity.Province;
import com.mapic.backend.service.ILocationService;
import com.mapic.backend.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final ILocationService locationService;

    @GetMapping("/provinces")
    public ResponseEntity<ApiResponse<List<Province>>> getAllProvinces() {
        return ResponseEntity.ok(ApiResponse.success("Fetched all provinces", locationService.getAllProvinces()));
    }

    @GetMapping("/provinces/{provinceId}/districts")
    public ResponseEntity<ApiResponse<List<District>>> getDistricts(@PathVariable Integer provinceId) {
        return ResponseEntity.ok(ApiResponse.success("Fetched districts", locationService.getDistrictsByProvince(provinceId)));
    }

    @GetMapping("/districts/{districtId}/communes")
    public ResponseEntity<ApiResponse<List<Commune>>> getCommunes(@PathVariable Integer districtId) {
        return ResponseEntity.ok(ApiResponse.success("Fetched communes", locationService.getCommunesByDistrict(districtId)));
    }

    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<String>> seed() {
        locationService.seedLocations();
        return ResponseEntity.ok(ApiResponse.success("Location seeding triggered", "Successfully seeded sample locations"));
    }
}
