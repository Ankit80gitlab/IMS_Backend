package com.cms.incidentmanagement.controller;

import com.cms.incidentmanagement.configuration.ExceptionConfig;
import com.cms.incidentmanagement.dto.ZoneDto;
import com.cms.incidentmanagement.service.implementation.ZoneServiceImpl;
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
@RequestMapping("/zoneManagement")
public class ZoneController {
    private static final Logger logger = LoggerFactory.getLogger(ZoneController.class);
    @Autowired
    private ZoneServiceImpl zoneServiceImpl;
    @Autowired
    private ExceptionConfig exceptionConfig;

    @PostMapping("/addZone")
    public HashMap<String, Object> addZone(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody ZoneDto zoneDto) {
        HashMap<String, Object> map;
        try {
            map = zoneServiceImpl.addZone(zoneDto, token);
        } catch (Exception e) {
            logger.error("error: " + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @GetMapping("/getAllZones")
    public HashMap<String, Object> getAllZones(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "searchByName", required = false) String searchByName,
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        HashMap<String, Object> map;
        try {
            map = zoneServiceImpl.getAllZones(pageNo, pageSize, token, searchByName);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @PutMapping("/updateZone")
    public HashMap<String, Object> updateZone(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody ZoneDto zoneDto) {
        HashMap<String, Object> map;
        try {
            map = zoneServiceImpl.updateZone(zoneDto, token);
        } catch (Exception e) {
            logger.error("error : " + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @DeleteMapping("/deleteZone")
    public HashMap<String, Object> removeProduct(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "zoneId", required = false) Integer zoneId) {
        HashMap<String, Object> map;
        try {
            map = zoneServiceImpl.removeZone(zoneId);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @GetMapping("/getFullZoneInformations")
    public HashMap<String, Object> getZoneInformations(
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        HashMap<String, Object> map;
        try {
            map = zoneServiceImpl.getZoneInformations(pageNo, pageSize);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @GetMapping("/getZoneAreaInformations")
    public HashMap<String, Object> getZoneAreaInformations(
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        HashMap<String, Object> map;
        try {
            map = zoneServiceImpl.getZoneAreaInformations(pageNo, pageSize);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @GetMapping("/getAllZonesBasicDetails")
    public HashMap<String, Object> getAllZonesBasicDetails(

            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "searchByName", required = false) String searchByName,
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        HashMap<String, Object> map;
        try {
            map = zoneServiceImpl.getAllZonesBasicDetails(token, searchByName, pageNo, pageSize);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;


    }
}
