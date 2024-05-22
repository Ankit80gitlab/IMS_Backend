package com.cms.incidentmanagement.service;

import com.cms.incidentmanagement.dto.RoleDto;

import java.util.*;

public interface RoleManagementService {
	HashMap<String, Object> getAllRoles(Integer pageNo, Integer pageSize);

	HashMap<String, Object> createNewRole(RoleDto roleDto);

	HashMap<String, Object> deleteRole(int roleId);

	HashMap<String,Object> updateRole(RoleDto roleDto);

	HashMap<String, Object> getAllFeatures(Integer pageNo, Integer pageSize);

	HashMap<String, Object> getAllRolesBasicDetails(String token,String searchByName, Integer pageNo, Integer pageSize);

	HashMap<String,Object> getRoles();
}
