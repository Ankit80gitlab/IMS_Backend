package com.cms.core.repository;

import com.cms.core.entity.Area;
import com.cms.core.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Created by Shashidhar on 5/14/2024.
 */
public interface AreaRepository extends JpaRepository<Area,Integer>, JpaSpecificationExecutor<Area> {
    List<Area> findByZoneId(Integer id);

    long countByNameIgnoreCaseAndZoneId(String name, Integer zoneId);

    long countByNameIgnoreCaseAndZoneIdIsNull(String name);
}
