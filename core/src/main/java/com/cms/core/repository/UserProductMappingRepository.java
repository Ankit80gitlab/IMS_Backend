package com.cms.core.repository;

import com.cms.core.entity.Ticket;
import com.cms.core.entity.UserProductMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by Shashidhar on 4/25/2024.
 */
public interface UserProductMappingRepository extends JpaRepository<UserProductMapping, Integer> {
    List<UserProductMapping> findByCustomerProductMappingId(Integer existingustomerProductMappingId);
}
