package com.cms.core.repository;

import com.cms.core.entity.Customer;
import com.cms.core.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by Shashidhar on 4/15/2024.
 */

public interface CustomerRepository extends JpaRepository<Customer,Integer>, JpaSpecificationExecutor<Customer> {

	Customer findByName(String name);

	long countByNameIgnoreCase(String name);
}
