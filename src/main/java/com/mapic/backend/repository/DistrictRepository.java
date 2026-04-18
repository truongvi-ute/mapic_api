package com.mapic.backend.repository;

import com.mapic.backend.entity.District;
import com.mapic.backend.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<District, Integer> {
    List<District> findByProvinceId(Integer provinceId);
    Optional<District> findByNameAndProvince(String name, Province province);
}
