package com.mapic.backend.repository;

import com.mapic.backend.entity.Commune;
import com.mapic.backend.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommuneRepository extends JpaRepository<Commune, Integer> {
    List<Commune> findByDistrictId(Integer districtId);
    Optional<Commune> findByNameAndDistrict(String name, District district);
}
