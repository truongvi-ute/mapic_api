package com.mapic.backend.service;

import com.mapic.backend.entity.Province;
import com.mapic.backend.entity.District;
import com.mapic.backend.entity.Commune;

import java.util.List;

public interface ILocationService {
    List<Province> getAllProvinces();
    List<District> getDistrictsByProvince(Integer provinceId);
    List<Commune> getCommunesByDistrict(Integer districtId);
    
    // Seeding method
    void seedLocations();
}
