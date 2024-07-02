package com.cms.incidentmanagement.controller;

import com.cms.incidentmanagement.configuration.ExceptionConfig;
import com.cms.incidentmanagement.dto.DeviceDto;
import com.cms.incidentmanagement.service.implementation.DeviceManagementServiceImpl;
import com.cms.incidentmanagement.utility.Constant;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * Created by Shashidhar on 5/13/2024.
 */
@SecurityRequirement(name = Constant.BEARER_AUTH)
@RestController
@RequestMapping("/deviceManagement")
public class DeviceManagementController {
    private static final Logger logger = LoggerFactory.getLogger(DeviceManagementController.class);
    @Autowired
    private DeviceManagementServiceImpl deviceManagementServiceImpl;
    @Autowired
    private ExceptionConfig exceptionConfig;

    @PostMapping("/addDevice")
    public HashMap<String, Object> addDevice(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody DeviceDto devicedto) {
        HashMap<String, Object> map;
        try {

            map = deviceManagementServiceImpl.addDevice(devicedto, token);
        } catch (Exception e) {
            logger.error("error: " + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @GetMapping("/getAllDevices")
    public HashMap<String, Object> getAllDevices(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "searchByName", required = false) String searchByName,
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "areaId", required = false) Integer areaId) {
        HashMap<String, Object> map;
        try {
            map = deviceManagementServiceImpl.getAllDevices(pageNo, pageSize, searchByName, areaId, token);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @PutMapping("/updateDevice")
    public HashMap<String, Object> updateDevice(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody DeviceDto deviceDto) {
        HashMap<String, Object> map;
        try {
            map = deviceManagementServiceImpl.updateDevice(deviceDto, token);
        } catch (Exception e) {
            logger.error("error : " + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @DeleteMapping("/deleteDevice")
    public HashMap<String, Object> removeDevice(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "deviceId", required = false) Integer deviceId) {
        HashMap<String, Object> map;
        try {
            map = deviceManagementServiceImpl.removeDevice(deviceId);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @GetMapping("/getAllDevicesBasicDetails")
    public HashMap<String, Object> getAllDevicesBasicDetails(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "searchByName", required = false) String searchByName,
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "areaId", required = true) Integer areaId) {
        HashMap<String, Object> map;
        try {
            map = deviceManagementServiceImpl.getAllDevicesBasicDetails(searchByName, pageNo, pageSize, areaId, token);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @GetMapping("/getTotalDevices")
    public HashMap<String, Object> getTotalDevices(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "productId", required = true) Integer productId,
            @RequestParam(name = "searchByName", required = false) String searchByName,
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        HashMap<String, Object> map;
        try {
            map = deviceManagementServiceImpl.getTotalDevices(pageNo, pageSize, searchByName, productId, token);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }


}
