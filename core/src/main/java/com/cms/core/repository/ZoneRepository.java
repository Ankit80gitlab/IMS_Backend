package com.cms.core.repository;

import com.cms.core.entity.User;
import com.cms.core.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by Shashidhar on 5/13/2024.
 */
public interface ZoneRepository extends JpaRepository<Zone,Integer> ,JpaSpecificationExecutor<Zone> {
    long countByNameIgnoreCase(String name);
}
