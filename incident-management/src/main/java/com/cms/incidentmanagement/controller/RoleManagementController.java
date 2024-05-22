package com.cms.incidentmanagement.controller;

import com.cms.incidentmanagement.configuration.ExceptionConfig;
import com.cms.incidentmanagement.dto.RoleDto;
import com.cms.incidentmanagement.service.implementation.RoleManagementServiceImpl;
import com.cms.incidentmanagement.utility.Constant;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


@SecurityRequirement(name = Constant.BEARER_AUTH)
@RestController
@RequestMapping("/roleManagement")
public class RoleManagementController {

    private static final Logger logger = LoggerFactory.getLogger(RoleManagementController.class);
    @Autowired
    private RoleManagementServiceImpl roleManagementServiceImpl;
    @Autowired
    ExceptionConfig exceptionConfig;

    @RequestMapping(method = RequestMethod.GET, value = "/getAllRoles")
    public HashMap<String, Object> getAllRoles(
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        HashMap<String, Object> roleMap = new HashMap<>();
        try {
            roleMap = roleManagementServiceImpl.getAllRoles(pageNo, pageSize);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            roleMap = exceptionConfig.getTryCatchErrorMap(e);
        }
        return roleMap;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/createRole")
    public HashMap<String, Object> createNewRole(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody RoleDto roleDto) {
        HashMap<String, Object> createNewRoleMap;
        try {
            createNewRoleMap = roleManagementServiceImpl.createNewRole(roleDto);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            createNewRoleMap = exceptionConfig.getTryCatchErrorMap(e);
        }
        return createNewRoleMap;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/deleteRole")
    public HashMap<String, Object> deleteRole(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "roleId", required = false) Integer roleId) {
        HashMap<String, Object> deleteRoleMap;
        try {
            deleteRoleMap = roleManagementServiceImpl.deleteRole(roleId);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            deleteRoleMap = exceptionConfig.getTryCatchErrorMap(e);
        }
        return deleteRoleMap;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/updateRole")
    public HashMap<String, Object> updateUser(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody RoleDto roleDto) {
        HashMap<String, Object> updatedUserMap;
        try {
            updatedUserMap = roleManagementServiceImpl.updateRole(roleDto);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            updatedUserMap = exceptionConfig.getTryCatchErrorMap(e);
        }
        return updatedUserMap;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getAllFeatures")
    public HashMap<String, Object> getAllFeatureList(
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        HashMap<String, Object> getAllFeatureMap;
        try {
            getAllFeatureMap = roleManagementServiceImpl.getAllFeatures(pageNo, pageSize);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            getAllFeatureMap = exceptionConfig.getTryCatchErrorMap(e);
        }
        return getAllFeatureMap;
    }

    @GetMapping("/getRoles")
    public HashMap<String, Object> getRoles() {
        HashMap<String, Object> map;
        try {
            map = roleManagementServiceImpl.getRoles();
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }
    @GetMapping("/getAllRolesBasicDetails")
    public HashMap<String, Object> getAllRolesBasicDetails(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "searchByName", required = false) String searchByName,
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        HashMap<String, Object> map;
        try {
            map = roleManagementServiceImpl.getAllRolesBasicDetails(token, searchByName, pageNo, pageSize);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }


}
