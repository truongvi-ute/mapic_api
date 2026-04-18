package com.mapic.backend.repository;

import com.mapic.backend.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Integer> {
    Optional<Province> findByCode(String code);
    Optional<Province> findByName(String name);
}
