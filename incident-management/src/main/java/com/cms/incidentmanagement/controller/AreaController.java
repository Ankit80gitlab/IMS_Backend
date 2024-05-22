package com.cms.incidentmanagement.controller;

import com.cms.incidentmanagement.configuration.ExceptionConfig;
import com.cms.incidentmanagement.dto.AreaDto;
import com.cms.incidentmanagement.dto.ZoneDto;
import com.cms.incidentmanagement.service.implementation.AreaServiceImpl;
import com.cms.incidentmanagement.utility.Constant;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * Created by Shashidhar on 5/14/2024.
 */
@SecurityRequirement(name = Constant.BEARER_AUTH)
@RestController
@RequestMapping("/areaManagement")
public class AreaController {
    private static final Logger logger = LoggerFactory.getLogger(AreaController.class);
    @Autowired
    private ExceptionConfig exceptionConfig;
    @Autowired
    private AreaServiceImpl areaServiceImpl;

    @PostMapping("/addArea")
    public HashMap<String, Object> addArea(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody AreaDto areaDto) {
        HashMap<String, Object> map;
        try {
            map = areaServiceImpl.addArea(areaDto, token);
        } catch (Exception e) {
            logger.error("error: " + e.getMessage());
            map =  exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }
    @GetMapping("/getAllAreas")
    public HashMap<String, Object> getAllAreas(
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        HashMap<String, Object> map;
        try {
            map = areaServiceImpl.getAllAreas(pageNo, pageSize);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }
    @PutMapping("/updateArea")
    public HashMap<String, Object> updateArea(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody AreaDto areaDto) {
        HashMap<String, Object> map;
        try {
            map = areaServiceImpl.updateArea(areaDto, token);
        } catch (Exception e) {
            logger.error("error : " + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }


    @DeleteMapping("/deleteArea")
    public HashMap<String, Object> removeArea(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "areaId", required = false) Integer areaId) {
        HashMap<String, Object> map;
        try {
            map = areaServiceImpl.removeArea(areaId);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }
    @GetMapping("/getAllAreasBasicDetails")
    public HashMap<String, Object> getAllAreasBasicDetails(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "searchByName", required = false) String searchByName,
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        HashMap<String, Object> map;
        try {
            map = areaServiceImpl.getAllAreasBasicDetails(token, searchByName, pageNo, pageSize);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

}
