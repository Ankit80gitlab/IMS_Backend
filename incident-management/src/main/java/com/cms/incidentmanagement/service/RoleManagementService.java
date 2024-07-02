package com.cms.incidentmanagement.service;

import com.cms.incidentmanagement.dto.RoleDto;

import java.util.*;

public interface RoleManagementService {
    HashMap<String, Object> getAllRoles(Integer pageNo, Integer pageSize, String token, String searchByNamr);

    HashMap<String, Object> createNewRole(RoleDto roleDto, String token);

    HashMap<String, Object> deleteRole(int roleId);

    HashMap<String, Object> updateRole(RoleDto roleDto,String Token);

    HashMap<String, Object> getAllFeatures(Integer pageNo, Integer pageSize);

    HashMap<String, Object> getAllRolesBasicDetails(String token);

}
