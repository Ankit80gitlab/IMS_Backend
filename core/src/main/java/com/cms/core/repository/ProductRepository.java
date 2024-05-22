
package com.cms.core.repository;

import com.cms.core.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Shashidhar on 4/15/2024.
 */
public interface ProductRepository extends JpaRepository<Product, Integer>,JpaSpecificationExecutor<Product> {


    Page<Product> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    Product findByName(String name);


    long countByNameIgnoreCase(String productName);
}


