package com.cms.incidentmanagement.controller;


import com.cms.incidentmanagement.configuration.ExceptionConfig;
import com.cms.incidentmanagement.dto.UserDto;
import com.cms.incidentmanagement.service.implementation.UserManagementServiceImpl;
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
@RequestMapping("/userManagement")
public class UserManagementController {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);
    @Autowired
    UserManagementServiceImpl userManagementServiceImpl;
    @Autowired
    ExceptionConfig exceptionConfig;

    @RequestMapping(method = RequestMethod.POST, value = "/createUser")
    public HashMap<String, Object> registerUser(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody UserDto userDto) {
        HashMap<String, Object> registerUserMap;
        try {
            registerUserMap = userManagementServiceImpl.registerUser(userDto, token);
        } catch (Exception e) {
            logger.error("error: " + e.getMessage());
            registerUserMap = exceptionConfig.getTryCatchErrorMap(e);
        }
        return registerUserMap;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getAllUser")
    public HashMap<String, Object> getAllUsers(
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        HashMap<String, Object> allUserMap;
        try {
            allUserMap = userManagementServiceImpl.getAllUsers(pageNo, pageSize);
        } catch (Exception e) {
            logger.error("error : " + e.getMessage());
            allUserMap = exceptionConfig.getTryCatchErrorMap(e);
        }
        return allUserMap;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/updateUser")
    public HashMap<String, Object> updateUser(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody UserDto userDto) {
        HashMap<String, Object> updatedUserMap;
        try {
            updatedUserMap = userManagementServiceImpl.updateUser(userDto);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            updatedUserMap = exceptionConfig.getTryCatchErrorMap(e);
        }
        return updatedUserMap;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/changePassword")
    @ResponseBody
    public HashMap<String, Object> changePassword(
            @RequestParam(name = "currentPassword") String currentPassword,
            @RequestParam(name = "newPassword") String newPassword,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token) {
        HashMap<String, Object> response;
        try {
            response = userManagementServiceImpl.changePassword(token, currentPassword, newPassword);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            response = exceptionConfig.getTryCatchErrorMap(e);
        }
        return response;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/removeUser")
    public HashMap<String, Object> removeUser(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "userId", required = false) Integer userId) {
        HashMap<String, Object> deleteUserMap;
        try {
            deleteUserMap = userManagementServiceImpl.removeUser(userId);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            deleteUserMap = exceptionConfig.getTryCatchErrorMap(e);
        }
        return deleteUserMap;
    }



    @GetMapping("/getAllUsersBasicDetails")
    public HashMap<String, Object> getAllUSersBasicDetails(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "searchByName", required = false) String searchByUsername,
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        HashMap<String, Object> map;
        try {
            map = userManagementServiceImpl.getAllUserBasicDetails(token, searchByUsername, pageNo, pageSize);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }
}
