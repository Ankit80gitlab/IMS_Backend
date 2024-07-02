package com.cms.core.repository;

import com.cms.core.entity.CustomerProductMappingDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by Shashidhar on 5/16/2024.
 */
public interface CustomerProductMappingDeviceRepository extends JpaRepository<CustomerProductMappingDevice,Integer> {
    Optional<CustomerProductMappingDevice> findByCustomerProductMappingIdAndDeviceId(Integer customerProductMappingId, Integer deviceId);

    Optional<CustomerProductMappingDevice> findByDeviceId(Integer id);
}
