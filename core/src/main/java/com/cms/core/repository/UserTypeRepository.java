package com.cms.core.repository;

import com.cms.core.entity.User;
import com.cms.core.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by Shashidhar on 6/18/2024.
 */
public interface UserTypeRepository extends JpaRepository<UserType,Integer>,JpaSpecificationExecutor<UserType> {
}
