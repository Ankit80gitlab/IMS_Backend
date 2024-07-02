package com.cms.core.repository;

import com.cms.core.entity.Device;
import com.cms.core.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Created by Shashidhar on 5/13/2024.
 */
public interface ZoneRepository extends JpaRepository<Zone,Integer> ,JpaSpecificationExecutor<Zone> {
    long countByNameIgnoreCase(String name);

    Zone findByName(String name);

    Optional<Zone> findByCustomerId(Integer customerId);
}
