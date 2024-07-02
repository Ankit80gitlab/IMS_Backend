package com.cms.core.repository;

import com.cms.core.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role, Integer>,JpaSpecificationExecutor<Role> {
	Role findOneByName(String name);

	long countByNameIgnoreCaseAndCustomerId(String roleName, Integer id);

	long countByNameIgnoreCaseAndCustomerIdIsNull(String roleName);
}
