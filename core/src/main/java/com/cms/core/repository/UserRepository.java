package com.cms.core.repository;

import com.cms.core.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer>,JpaSpecificationExecutor<User> {

	User findByUsername(String username);

	long countByUsernameIgnoreCase(String username);

	long countByEmailIgnoreCase(String email);
}
