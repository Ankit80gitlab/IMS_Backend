package com.cms.core.repository;

import com.cms.core.entity.AreaDeviceMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AreaDeviceMappingRepository extends JpaRepository<AreaDeviceMapping,Integer> {

    Optional<AreaDeviceMapping> findByDeviceId(Integer id);
}
