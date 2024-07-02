package com.cms.core.repository;

import com.cms.core.entity.Area;
import com.cms.core.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by Shashidhar on 5/13/2024.
 */
public interface DeviceRepository extends JpaRepository<Device,Integer>, JpaSpecificationExecutor<Device> {

	long countByUidIgnoreCase(String uid);
}
