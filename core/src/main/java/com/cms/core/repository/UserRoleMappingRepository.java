package com.cms.core.repository;

import com.cms.core.entity.RoleFeatureMapping;
import com.cms.core.entity.UserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, Integer> {
    List<UserRoleMapping> findByRoleId(Integer roleId);
    List<UserRoleMapping> findByUserId(Integer userId);
}
