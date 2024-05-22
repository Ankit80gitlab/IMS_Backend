package com.cms.core.repository;


import com.cms.core.entity.Customer;
import com.cms.core.entity.CustomerProductMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by Shashidhar on 4/24/2024.
 */
public interface CustomerProductMappingRepository extends JpaRepository<CustomerProductMapping, Integer> {

    List<CustomerProductMapping> findByCustomerIdAndProductIdIn(Integer customerId, List<Integer> productIds);
    boolean existsByProductId(Integer productId);

    List<CustomerProductMapping> findBycustomerId(Integer customerId);
}
